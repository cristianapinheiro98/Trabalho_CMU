package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.UserType
import pt.ipp.estg.trabalho_cmu.data.repository.UserRepository
import java.security.MessageDigest

open class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        UserRepository(db.userDao())
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _isAuthenticated = MutableLiveData(false)
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    private val _isRegistered = MutableLiveData(false)
    val isRegistered: LiveData<Boolean> = _isRegistered

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    val name = MutableLiveData("")
    val address = MutableLiveData("")
    val phone = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val userType = MutableLiveData(UserType.UTILIZADOR)

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    fun login() = viewModelScope.launch {
        val emailValue = email.value?.trim().orEmpty()
        val passwordValue = password.value?.trim().orEmpty()

        if (emailValue.isBlank() || passwordValue.isBlank()) {
            _error.value = "Preenche todos os campos."
            return@launch
        }

        try {
            _isLoading.value = true
            val user = userRepository.getUserByEmail(emailValue)
            val hashedPassword = hashPassword(passwordValue)

            if (user != null && user.password == passwordValue) {
                _currentUser.value = user
                _isAuthenticated.value = true
                _message.value = "Login efetuado com sucesso!"
                _error.value = null
            } else {
                _error.value = "Credenciais inválidas."
                _isAuthenticated.value = false
                _currentUser.value = null
            }
        } catch (e: Exception) {
            _error.value = "Erro ao fazer login: ${e.message}"
            _currentUser.value = null
        } finally {
            _isLoading.value = false
        }
    }

    fun register() = viewModelScope.launch {
        val nameValue = name.value?.trim().orEmpty()
        val adressValue = address.value?.trim().orEmpty()
        val phoneValue = phone.value?.trim().orEmpty()
        val emailValue = email.value?.trim().orEmpty()
        val passwordValue = password.value?.trim().orEmpty()
        val userTypeValue = userType.value ?: UserType.UTILIZADOR

        if (nameValue.isBlank() || adressValue.isBlank() || phoneValue.isBlank() ||
            emailValue.isBlank() || passwordValue.isBlank()
        ) {
            _error.value = "Preenche todos os campos obrigatórios."
            return@launch
        }

        val phoneRegex = Regex("^[29][0-9]{8}$")
        if (!phoneRegex.matches(phoneValue)) {
            _error.value = "Telefone inválido. Deve começar por 2 ou 9 e ter 9 dígitos."
            return@launch
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        if (!emailRegex.matches(emailValue)) {
            _error.value = "Email Inválido."
            return@launch
        }

        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$")
        if (!passwordRegex.matches(passwordValue)) {
            _error.value =
                "Password deve ter 8+ caracteres, incluir 1 maiúscula, 1 número e 1 símbolo."
            return@launch
        }

        try {
            _isLoading.value = true

            val existingUser = userRepository.getUserByEmail(emailValue)
            if (existingUser != null) {
                _error.value = "Já existe uma conta com este email."
                return@launch
            }

            val encryptedPassword = hashPassword(passwordValue)

            val newUser = User(
                name = nameValue,
                adress = adressValue,
                email = emailValue,
                phone = phoneValue,
                password = encryptedPassword,
                userType = userTypeValue
            )

            userRepository.registerUser(newUser)
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
        name.value = ""
        address.value = ""
        phone.value = ""
        email.value = ""
        password.value = ""
        userType.value = UserType.UTILIZADOR
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
    fun resetRegistration() { _isRegistered.value = false }

    fun getCurrentUserId(): Int {
        return _currentUser.value?.id ?: 0
    }
}
