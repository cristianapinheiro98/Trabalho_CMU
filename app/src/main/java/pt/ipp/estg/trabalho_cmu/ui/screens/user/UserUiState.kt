package pt.ipp.estg.trabalho_cmu.ui.screens.user

/**
 * Represents the various UI states for user-related operations, such as Profile management and Preferences.
 *
 * This sealed class is used to communicate the current status of asynchronous operations
 * (loading, updating, error) from the ViewModel to the UI layer.
 */
sealed class UserUiState {
    /**
     * Initial state before any operation triggers.
     */
    object Initial : UserUiState()

    /**
     * State indicating that a background operation (fetching or updating) is in progress.
     */
    object Loading : UserUiState()

    /**
     * State indicating a generic successful operation.
     */
    object Success : UserUiState()

    /**
     * Specific success state indicating the user profile has been successfully updated.
     */
    object UserUpdated : UserUiState()

    /**
     * State indicating an error occurred during an operation.
     *
     * @property message User-friendly error message.
     */
    data class Error(val message: String) : UserUiState()
}