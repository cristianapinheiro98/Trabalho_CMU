package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.UserType
import pt.ipp.estg.trabalho_cmu.data.repository.UserRepository

open class AuthViewModel(private val userRepository: UserRepository? = null) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _isAuthenticated = MutableLiveData(false)
    open val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    private val _isRegistered = MutableLiveData(false)
    val isRegistered: LiveData<Boolean> = _isRegistered

    // 🆕 CURRENT USER - Usuário logado
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    val nome = MutableLiveData("")
    val morada = MutableLiveData("")
    val telefone = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val tipoConta = MutableLiveData(UserType.UTILIZADOR)

    fun login() = viewModelScope.launch {
        val emailValue = email.value?.trim().orEmpty()
        val passwordValue = password.value?.trim().orEmpty()

        if (emailValue.isBlank() || passwordValue.isBlank()) {
            _error.value = "Preenche todos os campos."
            return@launch
        }

        try {
            _isLoading.value = true
            val user = userRepository?.getUserByEmail(emailValue)
            if (user != null && user.password == passwordValue) {
                _currentUser.value = user // 🆕 Guarda o usuário logado
                _isAuthenticated.value = true
                _message.value = "Login efetuado com sucesso!"
                _error.value = null
            } else {
                _error.value = "Credenciais inválidas."
                _isAuthenticated.value = false
                _currentUser.value = null // 🆕 Limpa usuário
            }
        } catch (e: Exception) {
            _error.value = "Erro ao fazer login: ${e.message}"
            _currentUser.value = null
        } finally {
            _isLoading.value = false
        }
    }

    fun register() = viewModelScope.launch {
        val nomeValue = nome.value?.trim().orEmpty()
        val moradaValue = morada.value?.trim().orEmpty()
        val telefoneValue = telefone.value?.trim().orEmpty()
        val emailValue = email.value?.trim().orEmpty()
        val passwordValue = password.value?.trim().orEmpty()
        val tipoContaValue = tipoConta.value ?: UserType.UTILIZADOR

        if (nomeValue.isBlank() || moradaValue.isBlank() || telefoneValue.isBlank() ||
            emailValue.isBlank() || passwordValue.isBlank()
        ) {
            _error.value = "Preenche todos os campos obrigatórios."
            return@launch
        }

        if (!emailValue.contains("@") || !emailValue.contains(".")) {
            _error.value = "Email inválido."
            return@launch
        }

        if (passwordValue.length < 4) {
            _error.value = "A palavra-passe deve ter pelo menos 4 caracteres."
            return@launch
        }

        try {
            _isLoading.value = true

            val existingUser = userRepository?.getUserByEmail(emailValue)
            if (existingUser != null) {
                _error.value = "Já existe uma conta com este email."
                return@launch
            }

            val newUser = User(
                name = nomeValue,
                adress = moradaValue,
                email = emailValue,
                phone = telefoneValue,
                password = passwordValue,
                userType = tipoContaValue
            )

            userRepository?.registerUser(newUser)
            _isRegistered.value = true
            _message.value = "Conta criada com sucesso!"
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Erro ao criar conta: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentUser.value = null
        clearFields()
    }

    fun clearFields() {
        nome.value = ""
        morada.value = ""
        telefone.value = ""
        email.value = ""
        password.value = ""
        tipoConta.value = UserType.UTILIZADOR
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
    fun resetRegistration() { _isRegistered.value = false }

    fun getCurrentUserId(): Int {
        return _currentUser.value?.id ?: 0
    }
}