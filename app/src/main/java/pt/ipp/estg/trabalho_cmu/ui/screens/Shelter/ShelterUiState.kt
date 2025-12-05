package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

/**
 * Estados para visualização de abrigos (público)
 */
sealed class ShelterUiState {
    object Initial : ShelterUiState()
    object Loading : ShelterUiState()
    object Success : ShelterUiState()
    object ShelterUpdated : ShelterUiState()
    data class Error(val message: String) : ShelterUiState()
}