package pt.ipp.estg.trabalho_cmu.ui.screens.User

/**
 * Estados para o ecrã principal (Dashboard)
 */
sealed class MainOptionsUiState {
    object Initial : MainOptionsUiState()
    object Loading : MainOptionsUiState()
    object Success : MainOptionsUiState()
    data class Error(val message: String) : MainOptionsUiState()
}