package pt.ipp.estg.trabalho_cmu.ui.screens.user

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * UI State for Main Options Screen (User Dashboard)
 * Represents different states of the dashboard following MVVM pattern
 */
sealed class MainOptionsUiState {
    /**
     * Initial state before loading
     */
    object Initial : MainOptionsUiState()

    /**
     * Loading dashboard data
     */
    object Loading : MainOptionsUiState()

    /**
     * Successfully loaded dashboard data
     * @property animals User's owned animals
     * @property recentMedals Recently earned medals (last 5)
     * @property lastWalk Information about last completed walk
     */
    data class Success(
        val userName: String,
        val animals: List<Animal>,
        val recentMedals: List<MedalItem>,
        val lastWalk: LastWalkInfo?
    ) : MainOptionsUiState()

    /**
     * Error state
     * @property message Error message to display
     */
    data class Error(val message: String) : MainOptionsUiState()
}

/**
 * Dialog state managed by ViewModel
 * Controls visibility and data for all dialogs in MainOptionsScreen
 */
data class DialogState(
    val isAnimalSelectionVisible: Boolean = false,
    val isNoAnimalsVisible: Boolean = false,
    val isMedalCollectionVisible: Boolean = false,
    val dialogType: DialogType = DialogType.SCHEDULE,
    val availableAnimalsForWalk: List<Animal> = emptyList(),
    val isLoadingAnimals: Boolean = false
)

/**
 * Types of dialogs that can be shown for animal selection
 */
enum class DialogType {
    /** Dialog for scheduling a visit/activity */
    SCHEDULE,
    /** Dialog for starting a walk */
    START_WALK
}