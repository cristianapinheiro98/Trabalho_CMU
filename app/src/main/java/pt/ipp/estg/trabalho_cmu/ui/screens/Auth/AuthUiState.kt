package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import pt.ipp.estg.trabalho_cmu.data.models.LoginResult

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val loginResult: LoginResult, val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object TokenExpired : AuthUiState()
}