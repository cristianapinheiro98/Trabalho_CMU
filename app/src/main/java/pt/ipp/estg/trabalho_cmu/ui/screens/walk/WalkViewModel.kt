package pt.ipp.estg.trabalho_cmu.ui.screens.walk

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.UserRepository
import pt.ipp.estg.trabalho_cmu.data.repository.WalkRepository
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.services.WalkTrackingService
import pt.ipp.estg.trabalho_cmu.utils.MedalCalculator
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for Walk Screen.
 *
 * Manages walk tracking state and communication with [WalkTrackingService].
 * Handles two main scenarios:
 * 1. Starting a new walk with a specific animal
 * 2. Resuming an ongoing walk when returning from notification
 *
 * The ViewModel maintains a service connection to receive real-time location
 * updates and manages the UI state accordingly.
 *
 * @param application Application context for service binding and repository access
 */
class WalkViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "WalkViewModel"
    }

    private val context = application.applicationContext
    private val animalRepository: AnimalRepository
    private val walkRepository: WalkRepository

    private val userRepository: UserRepository

    init {
        val application = getApplication<Application>()
        animalRepository = DatabaseModule.provideAnimalRepository(application)
        walkRepository = DatabaseModule.provideWalkRepository(application)
        userRepository = DatabaseModule.provideUserRepository(application)
    }

    // UI State
    private val _uiState = MutableLiveData<WalkUiState>(WalkUiState.Initial)
    val uiState: LiveData<WalkUiState> = _uiState

    // Service connection
    private var trackingService: WalkTrackingService? = null
    private var isBound = false

    // Pending walk data (waiting for service connection)
    private var pendingAnimalId: String? = null
    private var pendingAnimalName: String? = null

    // Flag to indicate we're resuming from notification (not starting new walk)
    private var isResumingFromService = false

    /**
     * Service connection callback for binding to [WalkTrackingService].
     *
     * Handles two scenarios:
     * - New walk: Starts tracking with pending animal data
     * - Resume: Loads existing walk data from the running service
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WalkTrackingService.LocalBinder
            trackingService = binder.getService()
            isBound = true

            // Add listener for updates
            trackingService?.addListener(trackingListener)

            if (isResumingFromService) {
                // Resuming from notification - load existing walk data
                handleServiceResumeConnection()
            } else {
                // Starting new walk - use pending data
                pendingAnimalId?.let { animalId ->
                    pendingAnimalName?.let { animalName ->
                        trackingService?.startTracking(animalId, animalName)
                        Log.d(TAG, "Started tracking after service connected")
                        pendingAnimalId = null
                        pendingAnimalName = null
                    }
                }

                // Load current walk data if service is already tracking
                trackingService?.getCurrentWalkData()?.let { walkData ->
                    updateTrackingState(
                        walkData.routePoints.lastOrNull(),
                        walkData.routePoints,
                        walkData.distance,
                        walkData.duration
                    )
                }
            }

            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService?.removeListener(trackingListener)
            trackingService = null
            isBound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    /**
     * Listener for real-time walk tracking updates from the service.
     * Updates UI state with new location, distance, and duration data.
     */
    private val trackingListener = object : WalkTrackingService.WalkUpdateListener {
        override fun onLocationUpdate(location: LatLng, distance: Double, duration: Long) {
            val currentState = _uiState.value
            if (currentState is WalkUiState.Tracking) {
                val updatedRoutePoints = currentState.routePoints + location
                _uiState.postValue(
                    currentState.copy(
                        currentLocation = location,
                        routePoints = updatedRoutePoints,
                        distance = distance,
                        duration = duration
                    )
                )
            }
        }

        override fun onTrackingStateChanged(isTracking: Boolean) {
            Log.d(TAG, "Tracking state changed: $isTracking")
        }
    }

    /**
     * Load animal data and start a new walk tracking session.
     *
     * This method is called when the user initiates a walk from the animal
     * selection screen. It loads the animal data, initializes the tracking
     * state, and binds to the tracking service.
     *
     * @param animalId ID of the animal to walk
     */
    fun startWalk(animalId: String) {
        // Check internet connection
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.value = WalkUiState.Offline
            return
        }

        _uiState.value = WalkUiState.Loading
        isResumingFromService = false

        viewModelScope.launch {
            try {
                // Load animal data
                val animal = animalRepository.getAnimalById(animalId)
                if (animal == null) {
                    _uiState.value = WalkUiState.Error("Animal not found")
                    return@launch
                }

                // Initialize tracking state
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                _uiState.value = WalkUiState.Tracking(
                    animal = animal,
                    animalImageUrl = animal.imageUrls.firstOrNull() ?: "",
                    currentLocation = null,
                    routePoints = emptyList(),
                    distance = 0.0,
                    duration = 0L,
                    date = currentDate
                )

                // Store pending data for when service connects
                pendingAnimalId = animal.id
                pendingAnimalName = animal.name

                // Bind to service (tracking will start in onServiceConnected)
                bindService()

                // If service is already bound, start tracking immediately
                if (isBound && trackingService != null) {
                    trackingService?.startTracking(animal.id, animal.name)
                    pendingAnimalId = null
                    pendingAnimalName = null
                    Log.d(TAG, "Started tracking immediately (service already bound)")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error starting walk", e)
                _uiState.value = WalkUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Resume an ongoing walk from the tracking service.
     *
     * This method is called when the user returns to the walk screen from
     * the notification (either by tapping the notification or the "Stop Walk"
     * action button). It reconnects to the running service and restores the
     * UI state with the current walk data.
     *
     * If no walk is currently in progress, an error state is set.
     */
    fun resumeWalkFromService() {
        _uiState.value = WalkUiState.Loading
        isResumingFromService = true

        // Bind to existing service
        bindService()

        // If already bound, handle immediately
        if (isBound && trackingService != null) {
            handleServiceResumeConnection()
        }
    }

    /**
     * Handles the service connection when resuming a walk from notification.
     *
     * Retrieves current walk data from the service, loads the corresponding
     * animal data, and updates the UI state. If no active walk is found,
     * sets an error state.
     */
    private fun handleServiceResumeConnection() {
        viewModelScope.launch {
            try {
                val walkData = trackingService?.getCurrentWalkData()

                if (walkData == null || !trackingService!!.isCurrentlyTracking()) {
                    _uiState.postValue(WalkUiState.Error("No active walk found"))
                    return@launch
                }

                // Load animal data using animalId from service
                val animal = animalRepository.getAnimalById(walkData.animalId)
                if (animal == null) {
                    _uiState.postValue(WalkUiState.Error("Animal not found"))
                    return@launch
                }

                // Get current date
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                // Update UI state with current walk data
                _uiState.postValue(
                    WalkUiState.Tracking(
                        animal = animal,
                        animalImageUrl = animal.imageUrls.firstOrNull() ?: "",
                        currentLocation = walkData.routePoints.lastOrNull(),
                        routePoints = walkData.routePoints,
                        distance = walkData.distance,
                        duration = walkData.duration,
                        date = currentDate
                    )
                )

                Log.d(TAG, "Resumed walk from service: animalId=${walkData.animalId}, " +
                        "duration=${walkData.duration}s, distance=${walkData.distance}m")

            } catch (e: Exception) {
                Log.e(TAG, "Error resuming walk from service", e)
                _uiState.postValue(WalkUiState.Error(e.message ?: "Failed to resume walk"))
            }
        }
    }

    /**
     * Update tracking state with new location data.
     *
     * @param location Current GPS coordinates
     * @param routePoints List of all route coordinates
     * @param distance Total distance in meters
     * @param duration Total duration in seconds
     */
    private fun updateTrackingState(
        location: LatLng?,
        routePoints: List<LatLng>,
        distance: Double,
        duration: Long
    ) {
        val currentState = _uiState.value
        if (currentState is WalkUiState.Tracking) {
            _uiState.value = currentState.copy(
                currentLocation = location,
                routePoints = routePoints,
                distance = distance,
                duration = duration
            )
        }
    }

    /**
     * Stop tracking and return walk data for summary.
     *
     * Stops the tracking service and unbinds from it. The returned walk data
     * can be used to save the walk and navigate to the summary screen.
     *
     * @return Walk data containing all tracking information, or null if not tracking
     */
    fun stopWalk(): WalkTrackingService.WalkData? {
        val walkData = trackingService?.stopTracking()
        unbindService()
        return walkData
    }

    /**
     * Save completed walk to Firebase and local Room database.
     *
     * Creates a Walk entity with all tracking data including route points,
     * duration, distance, calculated medal, and denormalized user/animal
     * information for SocialTails community feed.
     *
     * @param walkData Walk data from tracking service
     * @param onSuccess Callback invoked with saved Walk entity on success
     * @param onError Callback invoked with error message on failure
     */
    fun saveWalk(
        walkData: WalkTrackingService.WalkData,
        onSuccess: (Walk) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Format route points as string
                val routePointsStr = walkData.routePoints.joinToString(";") {
                    "${it.latitude},${it.longitude}"
                }

                // Calculate medal
                val medal = MedalCalculator.calculateMedal(walkData.duration)

                // Get current date and time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val now = Date()
                val startDate = Date(now.time - (walkData.duration * 1000))

                // Get user info for denormalized fields
                val userId = getCurrentUserId()
                val user = userRepository.getUserById(userId)
                val userName = user?.name ?: ""

                // Get animal info for denormalized fields
                val animal = animalRepository.getAnimalById(walkData.animalId)
                val animalName = animal?.name ?: walkData.animalName
                val animalImageUrl = animal?.imageUrls?.firstOrNull() ?: ""

                val walk = Walk(
                    id = "",
                    userId = userId,
                    animalId = walkData.animalId,
                    date = dateFormat.format(now),
                    startTime = timeFormat.format(startDate),
                    endTime = timeFormat.format(now),
                    duration = walkData.duration,
                    distance = walkData.distance,
                    routePoints = routePointsStr,
                    medalType = medal?.name,
                    createdAt = System.currentTimeMillis(),
                    isPublic = false,
                    userName = userName,
                    animalName = animalName,
                    animalImageUrl = animalImageUrl
                )

                val result = walkRepository.createWalk(walk)

                result.onSuccess { savedWalk ->
                    Log.d(TAG, "Walk saved successfully: ${savedWalk.id}")
                    onSuccess(savedWalk)
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error saving walk", error)
                    onError(error.message ?: "Failed to save walk")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in saveWalk", e)
                onError(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Bind to WalkTrackingService.
     *
     * Starts the service if not running and binds to it for communication.
     * Uses BIND_AUTO_CREATE to ensure service is created if needed.
     */
    private fun bindService() {
        if (!isBound) {
            val intent = Intent(context, WalkTrackingService::class.java)
            context.startService(intent) // Ensure service is started
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Unbind from WalkTrackingService.
     *
     * Removes the tracking listener and disconnects from the service.
     * Should be called when the ViewModel is cleared or when tracking stops.
     */
    private fun unbindService() {
        if (isBound) {
            trackingService?.removeListener(trackingListener)
            context.unbindService(serviceConnection)
            isBound = false
            trackingService = null
        }
    }

    /**
     * Get current user ID from Firebase Auth.
     *
     * @return User ID string or empty string if not logged in
     */
    private fun getCurrentUserId(): String {
        return FirebaseProvider.auth.currentUser?.uid ?: ""
    }

    /**
     * Format distance for display.
     *
     * Converts meters to a human-readable string, showing kilometers
     * for distances >= 1000m.
     *
     * @param meters Distance in meters
     * @return Formatted string (e.g., "1.50 km" or "150 m")
     */
    fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            String.format("%.2f km", meters / 1000)
        } else {
            String.format("%.0f m", meters)
        }
    }

    /**
     * Format duration for display.
     *
     * Converts seconds to HH:mm:ss format.
     *
     * @param seconds Duration in seconds
     * @return Formatted string in HH:mm:ss format
     */
    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    /**
     * Cleanup when ViewModel is destroyed.
     *
     * Unbinds from the tracking service to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}