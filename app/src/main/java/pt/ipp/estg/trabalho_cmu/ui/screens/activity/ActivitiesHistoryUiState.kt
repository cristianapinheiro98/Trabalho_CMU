package pt.ipp.estg.trabalho_cmu.ui.screens.activity

import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

/**
 * Data class combining Activity with its related Animal and Shelter.
 *
 * Used to display complete activity information in the UI.
 *
 * @property activity The activity entity.
 * @property animal The animal involved in the activity.
 * @property shelter The shelter where the animal is located.
 */
data class ActivityWithDetails(
    val activity: Activity,
    val animal: Animal,
    val shelter: Shelter
)

/**
 * UI State for the Activities History Screen.
 *
 * Represents the different states the screen can be in while displaying activity history.
 */
sealed class ActivitiesHistoryUiState {

    /**
     * Initial state before any data is loaded.
     */
    object Initial : ActivitiesHistoryUiState()

    /**
     * Loading state while fetching activities from repositories.
     */
    object Loading : ActivitiesHistoryUiState()

    /**
     * Online success state with all activities loaded.
     * Shows ongoing, upcoming, and past activities.
     *
     * @property activities List of all activities with their details.
     */
    data class OnlineSuccess(
        val activities: List<ActivityWithDetails>
    ) : ActivitiesHistoryUiState()

    /**
     * Offline success state with cached activities.
     * Only shows activities from Room database with a warning message.
     * Recent activities might not be available.
     *
     * @property activities List of cached activities with their details.
     */
    data class OfflineSuccess(
        val activities: List<ActivityWithDetails>
    ) : ActivitiesHistoryUiState()

    /**
     * Error state when something goes wrong while loading activities.
     *
     * @property message Error message to display to the user.
     */
    data class Error(val message: String) : ActivitiesHistoryUiState()

    /**
     * Empty state when the user has no activities.
     */
    object Empty : ActivitiesHistoryUiState()
}
