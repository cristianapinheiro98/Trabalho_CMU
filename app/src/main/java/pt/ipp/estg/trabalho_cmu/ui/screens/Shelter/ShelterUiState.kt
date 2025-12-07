package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

/**
 * UI states for viewing shelters in the *public* section of the app.
 *
 * Used in:
 * - Shelter catalog
 * - Shelter details
 */
sealed class ShelterUiState {
    object Initial : ShelterUiState()
    object Loading : ShelterUiState()
    object Success : ShelterUiState()
    object ShelterUpdated : ShelterUiState()
    data class Error(val message: String) : ShelterUiState()
}