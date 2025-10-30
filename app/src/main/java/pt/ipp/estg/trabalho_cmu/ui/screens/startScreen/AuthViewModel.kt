package pt.ipp.estg.trabalho_cmu.ui.screens.startScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.models.UserType
import javax.inject.Inject

data class AuthUiState(
    val nome: String = "",
    val morada: String = "",
    val telefone: String = "",
    val email: String = "",
    val password: String = "",
    val tipoConta: UserType= UserType.UTILIZADOR,
    val showDialog: Boolean = false,
    val dialogMessage: String = "",
    val isSuccess: Boolean = false,
    val isAdmin: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onNomeChange(value: String) = update { copy(nome = value) }
    fun onMoradaChange(value: String) = update { copy(morada = value) }
    fun onTelefoneChange(value: String) = update { copy(telefone = value) }
    fun onEmailChange(value: String) = update { copy(email = value) }
    fun onPasswordChange(value: String) = update { copy(password = value) }
    fun onTipoContaChange(value: UserType) = update { copy(tipoConta = value) }

    private inline fun update(block: AuthUiState.() -> AuthUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun login() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.email.isBlank() || state.password.isBlank()) {
                showDialog("Por favor, preencha todos os campos.", false)
            } else {
                val isAdmin = state.email.contains("abrigo", ignoreCase = true)
                _uiState.value = state.copy(
                    showDialog = true,
                    dialogMessage = "Login efetuado com sucesso!",
                    isSuccess = true,
                    isAdmin = isAdmin
                )
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            val s = _uiState.value
            if (s.nome.isBlank() || s.morada.isBlank() || s.telefone.isBlank() ||
                s.email.isBlank() || s.password.isBlank()
            ) {
                showDialog("Por favor, preencha todos os campos.", false)
            } else {
                showDialog("Conta criada com sucesso!", true)
            }
        }
    }

    private fun showDialog(message: String, success: Boolean) {
        _uiState.value = _uiState.value.copy(
            showDialog = true,
            dialogMessage = message,
            isSuccess = success
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }
}
