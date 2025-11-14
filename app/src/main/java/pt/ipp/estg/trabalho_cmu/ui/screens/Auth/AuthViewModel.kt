package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.data.repository.AuthRepository

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        AuthRepository(
            userDao = db.userDao(),
            shelterDao = db.shelterDao(),
            firebaseAuth = Firebase.auth,
            firestore = Firebase.firestore
        )
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

    private val _currentShelter = MutableLiveData<Shelter?>()
    val currentShelter: LiveData<Shelter?> = _currentShelter

    private val _accountType = MutableLiveData<AccountType>(AccountType.NONE)
    val accountType: LiveData<AccountType> = _accountType

    val name = MutableLiveData("")
    val address = MutableLiveData("")
    val phone = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val accountTypeChoice = MutableLiveData(AccountType.USER)


    // ===== LOGIN =====
    fun login() = viewModelScope.launch {

        if (checkAndRestoreOfflineSession()) return@launch

        val emailValue = email.value?.trim().orEmpty()
        val passwordValue = password.value?.trim().orEmpty()

        if (!validateLoginFields(emailValue, passwordValue)) return@launch

        performOnlineLogin(emailValue, passwordValue)
    }

    private suspend fun checkAndRestoreOfflineSession(): Boolean {
        val offlineSession = authRepository.checkOfflineSession()
        if (offlineSession != null) {
            updateAuthState(
                user = offlineSession.user,
                shelter = offlineSession.shelter,
                accountType = offlineSession.accountType,
                message = "Sessão recuperada!"
            )
            viewModelScope.launch {
                try {
                    val db = AppDatabase.getDatabase(getApplication())
                    val ownershipRepo = pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository(
                        db.ownershipDao(),
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    )

                    ownershipRepo.fetchOwnerships()
                } catch (e: Exception) {
                    println("[Offline] Error: ${e.message}")
                    e.printStackTrace()
                }
            }
            return true
        }
        return false
    }

    private fun validateLoginFields(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Preenche todos os campos."
            return false
        }
        return true
    }

    private suspend fun performOnlineLogin(email: String, password: String) {
        try {
            _isLoading.value = true
            val result = authRepository.login(email, password)

            result.onSuccess { loginResult ->
                updateAuthState(
                    user = loginResult.user,
                    shelter = loginResult.shelter,
                    accountType = loginResult.accountType,
                    message = "Login efetuado com sucesso!"
                )
                viewModelScope.launch {
                    try {
                        val db = AppDatabase.getDatabase(getApplication())
                        val ownershipRepo = pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository(
                            db.ownershipDao(),
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        )

                        ownershipRepo.fetchOwnerships()
                    } catch (e: Exception) {
                        println("Error syncronizing ownerships: ${e.message}")
                        e.printStackTrace()
                    }
                }

            }.onFailure { exception ->
                handleLoginFailure(exception)
            }
        } catch (e: Exception) {
            handleLoginFailure(e)
        } finally {
            _isLoading.value = false
        }
    }

    private fun updateAuthState(
        user: User? = null,
        shelter: Shelter? = null,
        accountType: AccountType,
        message: String
    ) {
        _currentUser.value = user
        _currentShelter.value = shelter
        _accountType.value = accountType
        _isAuthenticated.value = true
        _message.value = message
        _error.value = null
    }

    private fun handleLoginFailure(exception: Throwable) {
        _error.value = "Erro ao fazer login: ${exception.message}"
        _isAuthenticated.value = false
        _currentUser.value = null
        _currentShelter.value = null
    }

    // ===== REGISTER =====

    fun register() = viewModelScope.launch {
        val basicFields = getBasicRegistrationFields()
        val accountType = accountTypeChoice.value ?: AccountType.USER

        if (!validateBasicFields(basicFields)) return@launch
        if (!validateCredentials(
                basicFields.email,
                basicFields.password,
                basicFields.contact
            )
        ) return@launch




        performRegistration(basicFields, accountType)
    }

    private data class BasicRegistrationFields(
        val name: String,
        val address: String,
        val contact: String,
        val email: String,
        val password: String
    )

    private fun getBasicRegistrationFields() = BasicRegistrationFields(
        name = name.value?.trim().orEmpty(),
        address = address.value?.trim().orEmpty(),
        contact = phone.value?.trim().orEmpty(),
        email = email.value?.trim().orEmpty(),
        password = password.value?.trim().orEmpty()
    )

    private fun validateBasicFields(fields: BasicRegistrationFields): Boolean {
        if (fields.name.isBlank() || fields.address.isBlank() ||
            fields.contact.isBlank() || fields.email.isBlank() || fields.password.isBlank()) {
            _error.value = "Preenche todos os campos obrigatórios."
            return false
        }
        return true
    }



    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!?.*()-_])[A-Za-z\\d@#\$%^&+=!?.*()-_]{6,}$")

        return passwordRegex.matches(password)
    }


    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 9 &&
                phone.all { it.isDigit() } &&
                (phone.startsWith("9") || phone.startsWith("2"))
    }


    private fun validateCredentials(email: String, password: String, phone: String): Boolean {

        if (!isValidEmail(email)) {
            _error.value = "Email inválido."
            return false
        }

        if (!isValidPassword(password)) {
            _error.value = "A palavra-passe deve ter pelo menos 6 caracteres, uma letra maiúscula, minúscula, um número e um caracter especial."
            return false
        }

        if (!isValidPhone(phone)) {
            _error.value = "O contacto deve ter 9 dígitos numéricos."
            return false
        }

        return true
    }



    private suspend fun performRegistration(fields: BasicRegistrationFields, accountType: AccountType) {
        try {
            _isLoading.value = true

            val result = if (accountType == AccountType.USER) {
                registerUserAccount(fields)
            } else {
                registerShelterAccount(fields)
            }

            result.onSuccess { entity ->
                if (accountType == AccountType.USER) {
                    _currentUser.value = entity as User
                    _accountType.value = AccountType.USER
                    _message.value = "Conta criada com sucesso!"
                } else {
                    _currentShelter.value = entity as Shelter
                    _accountType.value = AccountType.SHELTER
                    _message.value = "Abrigo criado com sucesso!"
                }

                _isRegistered.value = true
                _isAuthenticated.value = true
                _error.value = null
            }.onFailure { exception ->
                _error.value = "Erro ao criar conta: ${exception.message}"
            }
        } catch (e: Exception) {
            println("Erro no registo: ${e.message}")
            e.printStackTrace()
            _error.value = "Erro ao criar conta: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun registerUserAccount(fields: BasicRegistrationFields) : Result<Any> =
        authRepository.registerUser(
            name = fields.name,
            address = fields.address,
            phone = fields.contact,
            email = fields.email,
            password = fields.password
        )

    private suspend fun registerShelterAccount(fields: BasicRegistrationFields) : Result<Any> =
        authRepository.registerShelter(
            name = fields.name,
            address = fields.address,
            contact = fields.contact,
            email = fields.email,
            password = fields.password
        )

    // ===== UTILS =====

    fun logout() {
        authRepository.logout()
        _isAuthenticated.value = false
        _currentUser.value = null
        _currentShelter.value = null
        _accountType.value = AccountType.NONE
        clearFields()
    }

    fun clearFields() {
        name.value = ""
        address.value = ""
        phone.value = ""
        email.value = ""
        password.value = ""
        accountTypeChoice.value = AccountType.USER
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
    fun resetRegistration() { _isRegistered.value = false }

    fun getCurrentUserId(): Int = _currentUser.value?.id ?: 0

    fun isAdmin(): Boolean = _accountType.value == AccountType.SHELTER
}