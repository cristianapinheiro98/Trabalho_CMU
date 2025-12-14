package pt.ipp.estg.trabalho_cmu.ui.screens.Walk

import android.app.Application
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
import pt.ipp.estg.trabalho_cmu.utils.MedalCalculator

/**
 * ViewModel for Walk Summary Screen
 * Manages walk summary display and save/discard actions
 */
class WalkSummaryViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "WalkSummaryViewModel"
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
    private val _uiState = MutableLiveData<WalkSummaryUiState>(WalkSummaryUiState.Initial)
    val uiState: LiveData<WalkSummaryUiState> = _uiState

    /**
     * Load walk summary data
     * @param walk Walk entity to display
     */
    fun loadWalkSummary(walk: Walk) {
        _uiState.value = WalkSummaryUiState.Loading

        viewModelScope.launch {
            try {
                // Load animal data
                val animal = animalRepository.getAnimalById(walk.animalId)
                if (animal == null) {
                    _uiState.value = WalkSummaryUiState.Error("Animal not found")
                    return@launch
                }

                // Parse route points
                val routePoints = parseRoutePoints(walk.routePoints)

                // Format data for display
                val formattedDistance = formatDistance(walk.distance)
                val formattedDuration = formatDuration(walk.duration)

                // Get medal emoji if medal was earned
                val medalEmoji = walk.medalType?.let { medalType ->
                    try {
                        val medal = MedalCalculator.MedalType.valueOf(medalType)
                        MedalCalculator.getMedalEmoji(medal)
                    } catch (e: Exception) {
                        null
                    }
                }

                _uiState.value = WalkSummaryUiState.Success(
                    walk = walk,
                    animalName = animal.name,
                    animalImageUrl = animal.imageUrls.firstOrNull() ?: "",
                    routePoints = routePoints,
                    formattedDistance = formattedDistance,
                    formattedDuration = formattedDuration,
                    medalEmoji = medalEmoji
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error loading walk summary", e)
                _uiState.value = WalkSummaryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Load walk summary by ID from repository
     * @param walkId Walk ID to load
     */
    fun loadWalkSummaryById(walkId: String) {
        _uiState.value = WalkSummaryUiState.Loading

        viewModelScope.launch {
            try {
                val walk = walkRepository.getWalkById(walkId)
                if (walk == null) {
                    _uiState.value = WalkSummaryUiState.Error("Walk not found")
                    return@launch
                }

                loadWalkSummary(walk)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading walk by ID", e)
                _uiState.value = WalkSummaryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Save walk to personal history (already saved, just confirm)
     */
    fun saveToHistory() {
        _uiState.value = WalkSummaryUiState.SavedToHistory
    }

    /**
     * Discard walk (delete from Firebase and Room)
     * @param walkId Walk ID to delete
     */
    fun discardWalk(walkId: String) {
        viewModelScope.launch {
            try {
                val result = walkRepository.deleteWalk(walkId)

                result.onSuccess {
                    Log.d(TAG, "Walk discarded successfully")
                    _uiState.value = WalkSummaryUiState.Discarded
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error discarding walk", error)
                    _uiState.value = WalkSummaryUiState.Error(error.message ?: "Failed to discard walk")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in discardWalk", e)
                _uiState.value = WalkSummaryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Share walk to SocialTails community.
     *
     * Updates the walk's isPublic flag to true in both Firebase and Room,
     * making it visible in the community feed and eligible for podium rankings.
     * The walk is also saved to personal history.
     *
     * @param walkId Walk ID to share
     */
    fun shareToSocialTails(walkId: String) {
        viewModelScope.launch {
            try {
                val result = walkRepository.shareWalkToSocialTails(walkId)

                result.onSuccess {
                    Log.d(TAG, "Walk shared to SocialTails successfully")
                    _uiState.value = WalkSummaryUiState.SharedToSocialTails
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error sharing walk to SocialTails", error)
                    _uiState.value = WalkSummaryUiState.Error(
                        error.message ?: "Failed to share walk"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in shareToSocialTails", e)
                _uiState.value = WalkSummaryUiState.Error(e.message ?: "Unknown error")
            }
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
     * @return Formatted string (e.g., "1.50 km" or "150 m")
     */
    private fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            String.format("%.2f km", meters / 1000)
        } else {
            String.format("%.0f m", meters)
        }
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
}