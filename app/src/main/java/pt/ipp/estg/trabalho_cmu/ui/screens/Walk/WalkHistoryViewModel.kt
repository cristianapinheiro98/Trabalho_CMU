package pt.ipp.estg.trabalho_cmu.ui.screens.Walk

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.WalkRepository
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.MedalCalculator
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils

/**
 * ViewModel for Walk History Screen
 * Manages paginated walk history display
 */
class WalkHistoryViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "WalkHistoryViewModel"
        private const val PAGE_SIZE = 3 // 3 walks per page
    }

    private val context = application.applicationContext
    private val animalRepository: AnimalRepository
    private val walkRepository: WalkRepository

    init {
        val application = getApplication<Application>()
        animalRepository = DatabaseModule.provideAnimalRepository(application)
        walkRepository = DatabaseModule.provideWalkRepository(application)
    }

    // UI State
    private val _uiState = MutableLiveData<WalkHistoryUiState>(WalkHistoryUiState.Initial)
    val uiState: LiveData<WalkHistoryUiState> = _uiState

    // Pagination state
    private var currentPage = 0
    private val loadedWalks = mutableListOf<WalkHistoryItem>()

    /**
     * Load first page of walk history
     * @param scrollToWalkId Optional walk ID to scroll to (from medal collection)
     */
    fun loadWalkHistory(scrollToWalkId: String? = null) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.value = WalkHistoryUiState.Offline
        }

        _uiState.value = WalkHistoryUiState.Loading
        currentPage = 0
        loadedWalks.clear()

        viewModelScope.launch {
            try {
                // Sync walks from Firebase
                val userId = getCurrentUserId()
                if (NetworkUtils.isNetworkAvailable(context)) {
                    walkRepository.syncWalks(userId)
                }

                // Load first page
                loadPage(scrollToWalkId)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading walk history", e)
                _uiState.value = WalkHistoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Load next page of walks
     */
    fun loadMoreWalks() {
        val currentState = _uiState.value
        if (currentState !is WalkHistoryUiState.Success || !currentState.hasMore) {
            return
        }

        _uiState.value = WalkHistoryUiState.LoadingMore
        currentPage++

        viewModelScope.launch {
            loadPage()
        }
    }

    /**
     * Load a page of walks from database
     * @param scrollToWalkId Optional walk ID to scroll to
     */
    private suspend fun loadPage(scrollToWalkId: String? = null) {
        try {
            val userId = getCurrentUserId()
            val offset = currentPage * PAGE_SIZE

            val walks = walkRepository.getWalksByUserPaginated(userId, PAGE_SIZE, offset)

            if (walks.isEmpty() && currentPage == 0) {
                _uiState.value = WalkHistoryUiState.Empty
                return
            }

            // Convert walks to display items
            val walkItems = walks.mapNotNull { walk ->
                createWalkHistoryItem(walk)
            }

            loadedWalks.addAll(walkItems)

            _uiState.value = WalkHistoryUiState.Success(
                walks = loadedWalks.toList(),
                hasMore = walks.size == PAGE_SIZE,
                scrollToWalkId = scrollToWalkId
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error loading page", e)
            _uiState.value = WalkHistoryUiState.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Create walk history item from walk entity
     * @param walk Walk entity
     * @return WalkHistoryItem or null if animal not found
     */
    private suspend fun createWalkHistoryItem(walk: Walk): WalkHistoryItem? {
        return try {
            val animal = animalRepository.getAnimalById(walk.animalId) ?: return null

            val routePoints = parseRoutePoints(walk.routePoints)
            val formattedDistance = formatDistance(walk.distance)
            val formattedDuration = formatDuration(walk.duration)

            val medalEmoji = walk.medalType?.let { medalType ->
                try {
                    val medal = MedalCalculator.MedalType.valueOf(medalType)
                    MedalCalculator.getMedalEmoji(medal)
                } catch (e: Exception) {
                    null
                }
            }

            WalkHistoryItem(
                walkId = walk.id,
                animalName = animal.name,
                animalImageUrl = animal.imageUrls.firstOrNull() ?: "",
                date = walk.date,
                duration = formattedDuration,
                distance = formattedDistance,
                routePoints = routePoints,
                medalEmoji = medalEmoji
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating walk history item", e)
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
     * Format duration for display
     * @param seconds Duration in seconds
     * @return Formatted string in HH:mm:ss format
     */
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    /**
     * Get current user ID from Firebase Auth
     * @return User ID string or empty if not logged in
     */
    private fun getCurrentUserId(): String {
        return FirebaseProvider.auth.currentUser?.uid ?: ""
    }

    /**
     * Refresh walk history
     */
    fun refresh() {
        loadWalkHistory()
    }
}