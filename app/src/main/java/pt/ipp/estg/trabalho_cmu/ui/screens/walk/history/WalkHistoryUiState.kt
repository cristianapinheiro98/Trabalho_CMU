package pt.ipp.estg.trabalho_cmu.ui.screens.walk.history

import com.google.android.gms.maps.model.LatLng

/**
 * UI State for Walk History Screen
 * Represents different states when viewing walk history with pagination
 */
sealed class WalkHistoryUiState {
    /**
     * Initial state before loading
     */
    object Initial : WalkHistoryUiState()

    /**
     * Loading first page of walks
     */
    object Loading : WalkHistoryUiState()

    /**
     * Loading more walks (pagination)
     */
    object LoadingMore : WalkHistoryUiState()

    /**
     * Offline mode - showing cached data
     */
    object Offline : WalkHistoryUiState()

    /**
     * Successfully loaded walks
     * @property walks List of walk items for display
     * @property hasMore True if more walks can be loaded
     * @property scrollToWalkId Optional walk ID to scroll to (from medal collection)
     */
    data class Success(
        val walks: List<WalkHistoryItem>,
        val hasMore: Boolean,
        val scrollToWalkId: String? = null
    ) : WalkHistoryUiState()

    /**
     * No walks found
     */
    object Empty : WalkHistoryUiState()

    /**
     * Error state
     * @property message Error message to display
     */
    data class Error(val message: String) : WalkHistoryUiState()
}

/**
 * Walk history item for display in list
 * @property walkId Unique walk ID
 * @property animalName Name of the animal
 * @property animalImageUrl First image URL of the animal
 * @property date Date of the walk
 * @property duration Formatted duration (HH:mm:ss)
 * @property distance Formatted distance (X.XX km)
 * @property routePoints GPS coordinates for map display
 * @property medalEmoji Medal emoji or null if no medal
 */
data class WalkHistoryItem(
    val walkId: String,
    val animalName: String,
    val animalImageUrl: String,
    val date: String,
    val duration: String,
    val distance: String,
    val routePoints: List<LatLng>,
    val medalEmoji: String?
)
