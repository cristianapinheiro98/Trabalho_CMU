package pt.ipp.estg.trabalho_cmu.ui.screens.Walk

import com.google.android.gms.maps.model.LatLng
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * UI State for Walk Screen
 * Represents different states during walk tracking
 */
sealed class WalkUiState {
    /**
     * Initial state before loading
     */
    object Initial : WalkUiState()

    /**
     * Loading animal data
     */
    object Loading : WalkUiState()

    /**
     * No internet connection - cannot start walk
     */
    object Offline : WalkUiState()

    /**
     * Walk is active and tracking
     * @property animal Animal being walked
     * @property animalImageUrl First image URL of the animal
     * @property currentLocation Current GPS location
     * @property routePoints List of all route points
     * @property distance Total distance in meters
     * @property duration Total duration in seconds
     * @property date Current date formatted as "dd/MM/yyyy"
     */
    data class Tracking(
        val animal: Animal,
        val animalImageUrl: String,
        val currentLocation: LatLng?,
        val routePoints: List<LatLng>,
        val distance: Double,
        val duration: Long,
        val date: String
    ) : WalkUiState()

    /**
     * Error state
     * @property message Error message to display
     */
    data class Error(val message: String) : WalkUiState()
}
