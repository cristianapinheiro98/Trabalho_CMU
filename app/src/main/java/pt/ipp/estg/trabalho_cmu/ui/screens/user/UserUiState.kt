package pt.ipp.estg.trabalho_cmu.ui.screens.user

/**
 * Estados para operações de Utilizador (Perfil, Preferências)
 */
sealed class UserUiState {
    object Initial : UserUiState()
    object Loading : UserUiState()
    object Success : UserUiState()
    object UserUpdated : UserUiState()
    data class Error(val message: String) : UserUiState()
}