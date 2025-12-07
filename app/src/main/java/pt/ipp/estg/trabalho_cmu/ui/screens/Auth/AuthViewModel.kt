package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.LoginResult
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.data.repository.AuthRepository
import android.util.Log

/**
 * ViewModel responsible for handling:
 * - Login
 * - Registration (user & shelter)
 * - Session validation
 * - Managing authentication UI state
 *
 * It uses AndroidViewModel so string resources and context
 * can be accessed through getApplication().
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        AuthRepository(
            appContext = application,
            userDao = db.userDao(),
            shelterDao = db.shelterDao()
        )
    }


    // ===================== UI STATE =====================
    private val _uiState = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val uiState: LiveData<AuthUiState> = _uiState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // ===================== AUTH STATE =====================
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

    private val ctx = getApplication<Application>()

    // ===================== FORM FIELDS =====================
    val name = MutableLiveData("")
    val address = MutableLiveData("")
    val phone = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val accountTypeChoice = MutableLiveData(AccountType.USER)

    init {
        checkSession()
    }

    fun getCurrentUserFirebaseUid(): String? = authRepository.getCurrentUserId()

    fun isAdmin(): Boolean = _accountType.value == AccountType.SHELTER

    // ===================== CHECK SESSION =====================
    /**
     * Tries to restore the user session.
     * If valid, updates UI state with LoginResult.
     * If expired, forces logout.
     */
    private fun checkSession() = viewModelScope.launch {
        _isLoading.value = true
        _uiState.value = AuthUiState.Loading

        val result = authRepository.checkSession()

        result.onSuccess { loginResult ->
            _isLoading.value = false
            if (loginResult != null) {
                updateAuthState(
                    user = loginResult.user,
                    shelter = loginResult.shelter,
                    accountType = loginResult.accountType,
                    message = ctx.getString(R.string.login_success)
                )
            } else _uiState.value = AuthUiState.Idle
        }.onFailure {
            _isLoading.value = false
            _uiState.value = AuthUiState.TokenExpired
        }
    }

    // ===================== LOGIN =====================
    fun login() = viewModelScope.launch {
        val emailValue = email.value?.trim().orEmpty()
        val passwordValue = password.value?.trim().orEmpty()

        if (!validateLoginFields(emailValue, passwordValue)) return@launch

        _isLoading.value = true
        _uiState.value = AuthUiState.Loading

        authRepository.login(emailValue, passwordValue)
            .onSuccess { loginResult ->
                _isLoading.value = false
                updateAuthState(
                    user = loginResult.user,
                    shelter = loginResult.shelter,
                    accountType = loginResult.accountType,
                    message = ctx.getString(R.string.login_success_message)
                )
            }
            .onFailure { ex ->
                _isLoading.value = false
                handleLoginFailure(ex)
            }
    }

    private fun validateLoginFields(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _error.value = ctx.getString(R.string.empty_fields_error)
            return false
        }
        return true
    }

    // ===================== REGISTER =====================
    fun register() = viewModelScope.launch {
        val fields = getBasicRegistrationFields()
        val type = accountTypeChoice.value ?: AccountType.USER

        if (!validateBasicFields(fields)) return@launch
        if (!validateCredentials(fields.email, fields.password, fields.contact)) return@launch

        _isLoading.value = true
        _uiState.value = AuthUiState.Loading

        try {
            val result =
                if (type == AccountType.USER) {
                    authRepository.registerUser(
                        fields.name, fields.address, fields.contact,
                        fields.email, fields.password
                    ).map {
                        LoginResult(user = it, accountType = AccountType.USER)
                    }
                } else {
                    authRepository.registerShelter(
                        fields.name, fields.address, fields.contact,
                        fields.email, fields.password
                    ).map {
                        LoginResult(shelter = it, accountType = AccountType.SHELTER)
                    }
                }

            result.onSuccess { loginRes ->
                _isLoading.value = false

                updateAuthState(
                    user = loginRes.user,
                    shelter = loginRes.shelter,
                    accountType = loginRes.accountType,
                    message =
                        if (loginRes.accountType == AccountType.USER)
                            ctx.getString(R.string.register_success_message)
                        else ctx.getString(R.string.shelter_register_success)
                )
                _isRegistered.value = true

            }.onFailure { e ->
                _isLoading.value = false
                _error.value = ctx.getString(R.string.register_failure_message) + " ${e.message}"
                _uiState.value = AuthUiState.Error(_error.value!!)
            }

        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = ctx.getString(R.string.unknown_error)
            _uiState.value = AuthUiState.Error(ctx.getString(R.string.unknown_error))
        }
    }

    // ===================== UPDATE AUTH STATE =====================
    private fun updateAuthState(
        user: User? = null,
        shelter: Shelter? = null,
        accountType: AccountType,
        message: String
    ) {
        Log.d("AUTH_DEBUG", "AccountType = $accountType")

        _currentUser.value = user
        _currentShelter.value = shelter
        _accountType.value = accountType
        _isAuthenticated.value = true
        _message.value = message
        _error.value = null

        _uiState.value = AuthUiState.Success(
            LoginResult(user, shelter, accountType),
            message
        )
    }

    private fun handleLoginFailure(exception: Throwable) {
        val errorMsg = ctx.getString(R.string.login_failure_message) + " ${exception.message}"
        _error.value = errorMsg
        _isAuthenticated.value = false
        _currentUser.value = null
        _currentShelter.value = null
        _uiState.value = AuthUiState.Error(errorMsg)
    }

    // ===================== VALIDATIONS =====================
    private data class BasicRegistrationFields(
        val name: String,
        val address: String,
        val contact: String,
        val email: String,
        val password: String
    )

    private fun getBasicRegistrationFields() =
        BasicRegistrationFields(
            name.value!!.trim(),
            address.value!!.trim(),
            phone.value!!.trim(),
            email.value!!.trim(),
            password.value!!.trim()
        )

    private fun validateBasicFields(f: BasicRegistrationFields): Boolean {
        if (f.name.isBlank() || f.address.isBlank() || f.contact.isBlank()
            || f.email.isBlank() || f.password.isBlank()
        ) {
            _error.value = ctx.getString(R.string.required_fields_error)
            return false
        }
        return true
    }

    private fun validateCredentials(email: String, password: String, phone: String): Boolean {

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = ctx.getString(R.string.invalid_email_error)
            return false
        }

        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!?.*()-_])[A-Za-z\\d@#\$%^&+=!?.*()-_]{6,}$")
        if (!regex.matches(password)) {
            _error.value = ctx.getString(R.string.invalid_password_error)
            return false
        }

        if (phone.length != 9 || phone.any { !it.isDigit() }
            || !(phone.startsWith("9") || phone.startsWith("2"))
        ) {
            _error.value = ctx.getString(R.string.invalid_phone_error)
            return false
        }

        return true
    }

    // ===================== LOGOUT =====================
    fun logout() {
        authRepository.logout()
        _isAuthenticated.value = false
        _currentUser.value = null
        _currentShelter.value = null
        _accountType.value = AccountType.NONE
        _uiState.value = AuthUiState.Idle
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
}
