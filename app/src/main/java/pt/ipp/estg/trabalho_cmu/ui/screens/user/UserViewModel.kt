package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // 🔹 Simula o carregamento de um utilizador por email
    fun loadUserByEmail(email: String) = viewModelScope.launch {
        try {
            _isLoading.value = true
            delay(500) // simula latência

            if (email.isNotBlank()) {
                _user.value = User(
                    id = 1,
                    name = "Utilizador Demo",
                    email = email,
                    password = "1234"
                )
                _message.value = "Utilizador carregado com sucesso!"
                _error.value = null
            } else {
                _error.value = "Email inválido"
                _user.value = null
            }
        } catch (e: Exception) {
            _error.value = "Erro a carregar utilizador: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    // 🔹 Simula uma atualização de perfil
    fun updateUser(u: User) = viewModelScope.launch {
        try {
            _isLoading.value = true
            delay(500)
            _user.value = u.copy(name = u.name.ifBlank { "Nome Atualizado" })
            _message.value = "Perfil atualizado com sucesso!"
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Erro a atualizar: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}
