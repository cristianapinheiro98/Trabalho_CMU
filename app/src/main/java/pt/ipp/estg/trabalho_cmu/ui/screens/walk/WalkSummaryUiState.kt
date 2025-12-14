package pt.ipp.estg.trabalho_cmu.ui.screens.walk

import com.google.android.gms.maps.model.LatLng
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk

/**
 * UI State for Walk Summary Screen.
 *
 * Represents different states when viewing completed walk summary,
 * including saving to personal history or sharing to SocialTails community.
 */
sealed class WalkSummaryUiState {
    /**
     * Initial state before loading.
     */
    object Initial : WalkSummaryUiState()

    /**
     * Loading walk data.
     */
    object Loading : WalkSummaryUiState()

    /**
     * Successfully loaded walk summary.
     *
     * @property walk Walk entity with all data
     * @property animalName Name of the animal
     * @property animalImageUrl First image URL of the animal
     * @property routePoints Parsed list of GPS coordinates
     * @property formattedDistance Distance formatted for display (e.g., "1.50 km")
     * @property formattedDuration Duration formatted for display (e.g., "01:23:45")
     * @property medalEmoji Medal emoji or null if no medal
     */
    data class Success(
        val walk: Walk,
        val animalName: String,
        val animalImageUrl: String,
        val routePoints: List<LatLng>,
        val formattedDistance: String,
        val formattedDuration: String,
        val medalEmoji: String?
    ) : WalkSummaryUiState()

    /**
     * Walk saved successfully to personal history.
     */
    object SavedToHistory : WalkSummaryUiState()

    /**
     * Walk shared successfully to SocialTails community.
     * Also saves to personal history.
     */
    object SharedToSocialTails : WalkSummaryUiState()

    /**
     * Walk discarded successfully.
     */
    object Discarded : WalkSummaryUiState()

    /**
     * Error state.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : WalkSummaryUiState()
}