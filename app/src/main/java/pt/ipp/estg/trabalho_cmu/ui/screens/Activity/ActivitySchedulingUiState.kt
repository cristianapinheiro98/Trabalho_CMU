package pt.ipp.estg.trabalho_cmu.ui.screens.Activity

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

/**
 * UI State for the Activity Scheduling Screen.
 *
 * Represents the different states the screen can be in during the activity scheduling flow.
 */
sealed class ActivitySchedulingUiState {

    /**
     * Initial state before any data is loaded.
     */
    object Initial : ActivitySchedulingUiState()

    /**
     * Loading state while fetching data from repositories.
     */
    object Loading : ActivitySchedulingUiState()

    /**
     * Offline state when there's no internet connection.
     * User cannot schedule activities without internet.
     */
    object Offline : ActivitySchedulingUiState()

    /**
     * Success state with all necessary data loaded.
     *
     * @property animal The selected animal for the activity.
     * @property shelter The shelter where the animal is located.
     * @property bookedDates List of dates already booked for this animal (format: "dd/MM/yyyy").
     * @property selectedDates Set of dates selected by the user for the new activity.
     * @property pickupTime Time when the user will pick up the animal (format: "HH:mm").
     * @property deliveryTime Time when the user will return the animal (format: "HH:mm").
     * @property validationError Any validation error that occurred.
     */
    data class Success(
        val animal: Animal,
        val shelter: Shelter,
        val bookedDates: List<String>,
        val selectedDates: Set<String> = emptySet(),
        val pickupTime: String = "09:00",
        val deliveryTime: String = "18:00",
        val validationError: ValidationError? = null
    ) : ActivitySchedulingUiState()

    /**
     * Error state when something goes wrong.
     *
     * @property message Error message to display to the user.
     */
    data class Error(val message: String) : ActivitySchedulingUiState()

    /**
     * Success state after scheduling an activity.
     * Triggers navigation back to the previous screen.
     */
    object SchedulingSuccess : ActivitySchedulingUiState()
}

/**
 * Validation errors that can occur during activity scheduling.
 */
sealed class ValidationError {
    /**
     * The selected time is outside the shelter's opening hours.
     */
    object TimeOutsideOpeningHours : ValidationError()

    /**
     * The selected dates conflict with an existing activity.
     */
    object DateConflict : ValidationError()

    /**
     * The activity must be scheduled at least 24 hours in advance.
     */
    object LessThan24Hours : ValidationError()

    /**
     * There's already an active activity for this animal.
     */
    object ActiveActivityExists : ValidationError()

    /**
     * No date was selected.
     */
    object NoDateSelected : ValidationError()
}
