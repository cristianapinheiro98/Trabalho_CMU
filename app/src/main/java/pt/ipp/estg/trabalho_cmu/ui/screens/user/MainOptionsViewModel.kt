package pt.ipp.estg.trabalho_cmu.ui.screens.user

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk
import pt.ipp.estg.trabalho_cmu.data.repository.ActivityRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.UserRepository
import pt.ipp.estg.trabalho_cmu.data.repository.WalkRepository
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.services.WalkTrackingService
import pt.ipp.estg.trabalho_cmu.utils.MedalCalculator
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for Main Options Screen (User Dashboard)
 * Manages dashboard data: animals, active walks, medals, last walk info, and dialog states
 * Follows MVVM pattern - all state is managed here, Screen is stateless
 */
class MainOptionsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MainOptionsViewModel"
    }

    private val context = application.applicationContext
    private val animalRepository: AnimalRepository
    private val activityRepository: ActivityRepository
    private val walkRepository: WalkRepository

    private val userRepository: UserRepository

    private val ownershipRepository: OwnershipRepository

    init {
        val application = getApplication<Application>()
        animalRepository = DatabaseModule.provideAnimalRepository(application)
        activityRepository = DatabaseModule.provideActivityRepository(application)
        walkRepository = DatabaseModule.provideWalkRepository(application)
        userRepository = DatabaseModule.provideUserRepository(application)
        ownershipRepository = DatabaseModule.provideOwnershipRepository(application)
    }

    // ========== UI State ==========

    private val _uiState = MutableLiveData<MainOptionsUiState>(MainOptionsUiState.Initial)
    val uiState: LiveData<MainOptionsUiState> = _uiState

    // ========== Dialog State ==========

    private val _dialogState = MutableLiveData(DialogState())
    val dialogState: LiveData<DialogState> = _dialogState

    // ========== Active Walk State ==========

    private val _isWalkActive = MutableLiveData(false)
    val isWalkActive: LiveData<Boolean> = _isWalkActive

    private val _activeWalkAnimalId = MutableLiveData<String?>(null)
    val activeWalkAnimalId: LiveData<String?> = _activeWalkAnimalId

    // ========== Dashboard Data Loading ==========

    /**
     * Load all dashboard data including animals, medals, and last walk
     * Called when screen is first displayed
     */
    fun loadDashboardData() {
        _uiState.value = MainOptionsUiState.Loading

        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()

                // Sync user (required for walk foreign key)
                userRepository.syncSpecificUser(userId)

                // Sync animals (required for walk foreign key)
                animalRepository.syncUserOwnedAnimals(userId)

                // Sync approved ownerships with celebration status
                ownershipRepository.syncUserApprovedOwnershipsWithCelebration(userId)

                // Sync walks (now foreign keys are satisfied)
                walkRepository.syncWalks(userId)

                // Sync activities
                activityRepository.syncActivities(userId)

                // Load data from Room (after sync)
                val user = userRepository.getUserById(userId)
                val userName = user?.name ?: "User"

                // Check for uncelebrated approved ownership
                checkForCelebration(userId, userName)

                // Load user's owned animals
                val animals = animalRepository.getOwnedAnimals(userId)

                if (animals.isEmpty()) {
                    _uiState.value = MainOptionsUiState.NoAnimals(userName = userName)
                    return@launch
                }

                // Load recent medals (last 5)
                val recentMedals = walkRepository.getRecentMedals(userId, 5)
                val medalItems = recentMedals.mapNotNull { walk ->
                    createMedalItem(walk)
                }

                // Load last walk
                val lastWalk = walkRepository.getLastWalk(userId)
                val lastWalkInfo = lastWalk?.let { createLastWalkInfo(it) }

                _uiState.value = MainOptionsUiState.Success(
                    userName = userName,
                    animals = animals,
                    recentMedals = medalItems,
                    lastWalk = lastWalkInfo
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard", e)
                _uiState.value = MainOptionsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ========== Dialog Control Functions ==========

    /**
     * Handle schedule visit button click
     * Shows animal selection dialog for scheduling
     */
    fun onScheduleVisitClick() {
        _dialogState.value = _dialogState.value?.copy(
            isAnimalSelectionVisible = true,
            dialogType = DialogType.SCHEDULE,
            isLoadingAnimals = false
        )
    }

    /**
     * Handle start walk button click
     * Loads available animals and shows appropriate dialog
     */
    fun onStartWalkClick() {
        val currentDialogState = _dialogState.value ?: DialogState()

        // Show loading state
        _dialogState.value = currentDialogState.copy(
            isAnimalSelectionVisible = true,
            dialogType = DialogType.START_WALK,
            isLoadingAnimals = true
        )

        // Load animals available for walk
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                val currentDate = getCurrentDate()

                // Get all user's animals from active activities
                val animals = animalRepository.getOwnedAnimals(userId)

                // Filter animals with active activities
                val availableAnimals = animals.filter { animal ->
                    val activeActivities = activityRepository.getActiveActivitiesByAnimal(
                        animal.id,
                        currentDate
                    )
                    activeActivities.isNotEmpty()
                }

                if (availableAnimals.isEmpty()) {
                    // No animals available - show info dialog
                    _dialogState.value = DialogState(
                        isAnimalSelectionVisible = false,
                        isNoAnimalsVisible = true
                    )
                } else {
                    // Show animal selection with available animals
                    _dialogState.value = DialogState(
                        isAnimalSelectionVisible = true,
                        dialogType = DialogType.START_WALK,
                        availableAnimalsForWalk = availableAnimals,
                        isLoadingAnimals = false
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error getting available animals", e)
                _dialogState.value = DialogState(
                    isAnimalSelectionVisible = false,
                    isNoAnimalsVisible = true
                )
            }
        }
    }

    /**
     * Handle view medals collection button click
     * Shows medal collection dialog
     */
    fun onViewMedalsClick() {
        _dialogState.value = _dialogState.value?.copy(
            isMedalCollectionVisible = true
        )
    }

    /**
     * Dismiss all dialogs
     * Called when user cancels or dialog should be closed
     */
    fun dismissDialog() {
        _dialogState.value = DialogState()
    }

    /**
     * Dismiss only the no animals dialog
     */
    fun dismissNoAnimalsDialog() {
        _dialogState.value = _dialogState.value?.copy(
            isNoAnimalsVisible = false
        )
    }

    /**
     * Dismiss only the medal collection dialog
     */
    fun dismissMedalCollectionDialog() {
        _dialogState.value = _dialogState.value?.copy(
            isMedalCollectionVisible = false
        )
    }

    // ========== Navigation Helpers ==========

    /**
     * Get navigation route for animal selection (schedule)
     * @param animal Selected animal
     * @return Navigation route string
     */
    fun getScheduleNavigationRoute(animal: Animal): String {
        dismissDialog()
        return "ActivityScheduling/${animal.id}"
    }

    /**
     * Get navigation route for starting walk
     * @param animal Selected animal
     * @return Navigation route string
     */
    fun getWalkNavigationRoute(animal: Animal): String {
        dismissDialog()
        return "Walk/${animal.id}"
    }

    /**
     * Get navigation route for walk history with optional scroll target
     * @param walkId Optional walk ID to scroll to
     * @return Navigation route string
     */
    fun getWalkHistoryNavigationRoute(walkId: String? = null): String {
        dismissMedalCollectionDialog()
        return if (walkId != null) {
            "WalkHistory?scrollToWalkId=$walkId"
        } else {
            "WalkHistory"
        }
    }

    // ========== Active Walk Management ==========

    /**
     * Check if there's a walk currently in progress
     * @param trackingService Walk tracking service instance
     */
    fun checkActiveWalk(trackingService: WalkTrackingService?) {
        val isTracking = trackingService?.isCurrentlyTracking() ?: false
        _isWalkActive.value = isTracking

        if (isTracking) {
            _activeWalkAnimalId.value = trackingService?.getCurrentWalkData()?.animalId
        } else {
            _activeWalkAnimalId.value = null
        }
    }

    /**
     * Get navigation route for active walk
     * @return Navigation route string or null if no active walk
     */
    fun getActiveWalkNavigationRoute(): String? {
        return _activeWalkAnimalId.value?.let { animalId ->
            "Walk/$animalId"
        }
    }

    // ========== Helper Functions ==========

    /**
     * Create medal item from walk entity
     * @param walk Walk entity with medal data
     * @return MedalItem for display or null if invalid
     */
    private suspend fun createMedalItem(walk: Walk): MedalItem? {
        return try {
            val animal = animalRepository.getAnimalById(walk.animalId) ?: return null
            val medalType = walk.medalType?.let {
                MedalCalculator.MedalType.valueOf(it)
            } ?: return null

            MedalItem(
                walkId = walk.id,
                emoji = MedalCalculator.getMedalEmoji(medalType),
                animalName = animal.name,
                date = walk.date
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating medal item", e)
            null
        }
    }

    /**
     * Create last walk info from walk entity
     * @param walk Walk entity
     * @return LastWalkInfo for display or null if invalid
     */
    private suspend fun createLastWalkInfo(walk: Walk): LastWalkInfo? {
        return try {
            val animal = animalRepository.getAnimalById(walk.animalId) ?: return null
            val routePoints = parseRoutePoints(walk.routePoints)

            LastWalkInfo(
                animalName = animal.name,
                animalImageUrl = animal.imageUrls.firstOrNull() ?: "",
                distance = formatDistance(walk.distance),
                routePoints = routePoints
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating last walk info", e)
            null
        }
    }

    /**
     * Parse route points from string format to LatLng list
     * @param routePointsStr String in format "lat,lng;lat,lng;..."
     * @return List of LatLng coordinates
     */
    private fun parseRoutePoints(routePointsStr: String): List<LatLng> {
        if (routePointsStr.isEmpty()) return emptyList()

        return try {
            routePointsStr.split(";").mapNotNull { point ->
                val parts = point.split(",")
                if (parts.size == 2) {
                    val lat = parts[0].toDoubleOrNull()
                    val lng = parts[1].toDoubleOrNull()
                    if (lat != null && lng != null) {
                        LatLng(lat, lng)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing route points", e)
            emptyList()
        }
    }

    /**
     * Format distance for display
     * @param meters Distance in meters
     * @return Formatted string (e.g., "1.50 km")
     */
    private fun formatDistance(meters: Double): String {
        return String.format("%.2f km", meters / 1000)
    }

    /**
     * Get current user ID from Firebase Auth
     * @return User ID string or empty if not logged in
     */
    private fun getCurrentUserId(): String {
        return FirebaseProvider.auth.currentUser?.uid ?: ""
    }

    /**
     * Get current date in format dd/MM/yyyy
     * @return Formatted date string
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Check if there's an approved ownership that needs celebration
     */
    private suspend fun checkForCelebration(userId: String, userName: String) {
        try {
            val uncelebratedOwnership = ownershipRepository.getUncelebratedApprovedOwnership(userId)

            if (uncelebratedOwnership != null) {
                val animal = animalRepository.getAnimalById(uncelebratedOwnership.animalId)

                if (animal != null) {
                    pt.ipp.estg.trabalho_cmu.notifications.NotificationManager.notifyOwnershipAccepted(
                        context = context,
                        animalName = animal.name,
                        animalId = animal.id
                    )

                    _dialogState.postValue(
                        _dialogState.value?.copy(
                            isCelebrationVisible = true,
                            celebrationData = CelebrationData(
                                ownershipId = uncelebratedOwnership.id,
                                userName = userName,
                                animalName = animal.name,
                                animalImageUrl = animal.imageUrls.firstOrNull() ?: ""
                            )
                        ) ?: DialogState(
                            isCelebrationVisible = true,
                            celebrationData = CelebrationData(
                                ownershipId = uncelebratedOwnership.id,
                                userName = userName,
                                animalName = animal.name,
                                animalImageUrl = animal.imageUrls.firstOrNull() ?: ""
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for celebration", e)
        }
    }

    /**
     * Dismiss celebration dialog and mark ownership as celebrated
     */
    fun dismissCelebrationDialog() {
        val celebrationData = _dialogState.value?.celebrationData

        _dialogState.value = _dialogState.value?.copy(
            isCelebrationVisible = false,
            celebrationData = null
        )

        // Mark as celebrated in background
        celebrationData?.let { data ->
            viewModelScope.launch {
                ownershipRepository.markOwnershipAsCelebrated(data.ownershipId)
            }
        }
    }
}

/**
 * Medal item for display in dashboard and collection
 * @property walkId Unique walk ID for navigation
 * @property emoji Medal emoji representation
 * @property animalName Name of the animal walked
 * @property date Date of the walk
 */
data class MedalItem(
    val walkId: String,
    val emoji: String,
    val animalName: String,
    val date: String
)

/**
 * Last walk info for dashboard display
 * @property animalName Name of the animal walked
 * @property distance Formatted distance string
 * @property routePoints GPS coordinates for mini map
 */
data class LastWalkInfo(
    val animalName: String,
    val animalImageUrl: String,
    val distance: String,
    val routePoints: List<LatLng>
)