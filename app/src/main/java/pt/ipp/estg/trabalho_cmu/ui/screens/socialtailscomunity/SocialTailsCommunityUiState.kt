package pt.ipp.estg.trabalho_cmu.ui.screens.socialtailscomunity

import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk

/**
 * UI State for SocialTails Community Screen.
 *
 * Represents different states when viewing the community feed,
 * including podium rankings and public walks from all users.
 */
sealed class SocialTailsCommunityUiState {
    /**
     * Initial state before loading.
     */
    object Initial : SocialTailsCommunityUiState()

    /**
     * Loading community data from Firebase.
     */
    object Loading : SocialTailsCommunityUiState()

    /**
     * User is offline and cannot access community features.
     */
    object Offline : SocialTailsCommunityUiState()

    /**
     * Successfully loaded community data.
     *
     * @property topWalksAllTime Top 3 walks of all time for the podium
     * @property topWalksMonthly Top 3 walks of the current month for the podium
     * @property publicWalks List of public walks for the feed
     * @property currentMonthName Name of the current month for display
     * @property hasMoreWalks Whether there are more walks to load
     * @property isLoadingMore Whether currently loading more walks
     */
    data class Success(
        val topWalksAllTime: List<Walk>,
        val topWalksMonthly: List<Walk>,
        val publicWalks: List<Walk>,
        val currentMonthName: String,
        val hasMoreWalks: Boolean = true,
        val isLoadingMore: Boolean = false
    ) : SocialTailsCommunityUiState()

    /**
     * Error state.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : SocialTailsCommunityUiState()
}