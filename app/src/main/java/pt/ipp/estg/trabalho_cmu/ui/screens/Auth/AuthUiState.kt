package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import pt.ipp.estg.trabalho_cmu.data.models.LoginResult


/**
 * Represents the different UI states for the authentication flow.
 *
 * - Idle → No ongoing authentication action
 * - Loading → Authentication request in progress
 * - Success → User successfully authenticated/registered
 * - Error → Authentication failed, contains user-facing message
 * - TokenExpired → User session is invalid or expired
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val loginResult: LoginResult, val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object TokenExpired : AuthUiState()
}