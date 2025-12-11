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
import pt.ipp.estg.trabalho_cmu.utils.StringHelper

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

    /**
     * Authentication repository that encapsulates all auth-related
     * data sources (Firebase Auth, Room DAOs for User and Shelter).
     */
    private val authRepository: AuthRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        AuthRepository(
            appContext = application,
            userDao = db.userDao(),
            shelterDao = db.shelterDao()
        )
    }


    /**
     * Backing field representing the current authentication UI state.
     */
    private val _uiState = MutableLiveData<AuthUiState>(AuthUiState.Idle)

    /**
     * Public observable auth UI state for the UI layer.
     */
    val uiState: LiveData<AuthUiState> = _uiState

    /**
     * Backing field indicating whether an auth-related operation is in progress.
     */
    private val _isLoading = MutableLiveData(false)

    /**
     * Public observable loading flag for showing/hiding progress indicators.
     */
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Backing field for the latest error message, if any.
     */
    private val _error = MutableLiveData<String?>()
    /**
     * Public observable error message, used by the UI to show feedback.
     */
    val error: LiveData<String?> = _error

    /**
     * Backing field for transient informational messages (e.g. success).
     */
    private val _message = MutableLiveData<String?>()
    /**
     * Public observable message, typically shown as a snackbar or toast.
     */
    val message: LiveData<String?> = _message

    /**
     * Backing field indicating whether a user is currently authenticated.
     */
    private val _isAuthenticated = MutableLiveData(false)
    /**
     * Public observable flag for authentication status.
     */
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    /**
     * Backing field indicating whether the last registration attempt was successful.
     */
    private val _isRegistered = MutableLiveData(false)

    /**
     * Public observable flag to detect when registration has completed successfully.
     */
    val isRegistered: LiveData<Boolean> = _isRegistered

    /**
     * Backing field holding the currently logged-in user, if any.
     */
    private val _currentUser = MutableLiveData<User?>()
    /**
     * Public observable current user.
     */
    val currentUser: LiveData<User?> = _currentUser

    /**
     * Backing field holding the currently logged-in shelter, if any.
     */
    private val _currentShelter = MutableLiveData<Shelter?>()


    /**
     * Public observable current shelter.
     */
    val currentShelter: LiveData<Shelter?> = _currentShelter

    /**
     * Backing field representing the current account type (USER, SHELTER, NONE).
     */
    private val _accountType = MutableLiveData<AccountType>(AccountType.NONE)

    /**
     * Public observable account type of the current session.
     */
    val accountType: LiveData<AccountType> = _accountType

    /**
     * Application context shortcut, useful for accessing string resources.
     */
    private val ctx = getApplication<Application>()

    /**
     * LiveData representing the form fields.
     */
    val name = MutableLiveData("")
    val address = MutableLiveData("")
    val phone = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val accountTypeChoice = MutableLiveData(AccountType.USER)

    init {
        checkSession()
    }

    /**
     * Returns the Firebase UID of the currently authenticated user, if any.
     */
    fun getCurrentUserFirebaseUid(): String? = authRepository.getCurrentUserId()


    /**
     * Returns true if the current account type corresponds to a shelter
     * (i.e., admin-like behavior in the app).
     */

    fun isAdmin(): Boolean = _accountType.value == AccountType.SHELTER


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
                    message = StringHelper.getString(ctx, R.string.login_success)
                )
            } else _uiState.value = AuthUiState.Idle
        }.onFailure {
            _isLoading.value = false
            _uiState.value = AuthUiState.TokenExpired
        }
    }

    /**
     * Attempts to authenticate a user with the current email and password
     * values stored in [email] and [password].
     *
     * Validates fields before calling the repository.
     * Updates UI state and error/message LiveData accordingly.
     */
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
                    message = StringHelper.getString(ctx, R.string.login_success_message)
                )
            }
            .onFailure { ex ->
                _isLoading.value = false
                handleLoginFailure(ex)
            }
    }

    /**
     * Validates the login fields (email and password).
     *
     * @return true if both fields are valid, false otherwise.
     */
    private fun validateLoginFields(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _error.value = StringHelper.getString(ctx, R.string.empty_fields_error)
            return false
        }
        return true
    }

    /**
     * Registers a new account (user or shelter) using the current form fields.
     *
     * Performs:
     * - Basic field validation
     * - Credential validation (email format, password strength, phone)
     * - Calls the appropriate repository register method based on account type
     * - Updates auth state and UI state on success or failure
     */
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
                            StringHelper.getString(ctx, R.string.register_success_message)
                        else StringHelper.getString(ctx, R.string.shelter_register_success)
                )
                _isRegistered.value = true

            }.onFailure { e ->
                _isLoading.value = false
                _error.value = StringHelper.getString(ctx, R.string.register_failure_message) + " ${e.message}"
                _uiState.value = AuthUiState.Error(_error.value!!)
            }

        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = StringHelper.getString(ctx, R.string.unknown_error)
            _uiState.value = AuthUiState.Error(StringHelper.getString(ctx,R.string.unknown_error))
        }
    }


    /**
     * Updates the ViewModel's internal authentication state and emits
     * a [AuthUiState.Success] result with the given [LoginResult] data.
     */
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

    /**
     * Handles login failures by setting an appropriate error message
     * and resetting authentication-related LiveData.
     */
    private fun handleLoginFailure(exception: Throwable) {
        val errorMsg = StringHelper.getString(ctx, R.string.login_failure_message) + " ${exception.message}"
        _error.value = errorMsg
        _isAuthenticated.value = false
        _currentUser.value = null
        _currentShelter.value = null
        _uiState.value = AuthUiState.Error(errorMsg)
    }


    /**
     * Helper data class that groups basic registration fields.
     */
    private data class BasicRegistrationFields(
        val name: String,
        val address: String,
        val contact: String,
        val email: String,
        val password: String
    )


    /**
     * Reads and trims the current registration fields into a [BasicRegistrationFields] instance.
     */
    private fun getBasicRegistrationFields() =
        BasicRegistrationFields(
            name.value!!.trim(),
            address.value!!.trim(),
            phone.value!!.trim(),
            email.value!!.trim(),
            password.value!!.trim()
        )


    /**
     * Validates that all basic registration fields are non-empty.
     */
    private fun validateBasicFields(f: BasicRegistrationFields): Boolean {
        if (f.name.isBlank() || f.address.isBlank() || f.contact.isBlank()
            || f.email.isBlank() || f.password.isBlank()
        ) {
            _error.value = StringHelper.getString(ctx, R.string.required_fields_error)
            return false
        }
        return true
    }


    /**
     * Validates the email, password and phone according to app rules.
     *
     * @return true if all credentials are valid, false otherwise.
     */
    private fun validateCredentials(email: String, password: String, phone: String): Boolean {

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = StringHelper.getString(ctx, R.string.invalid_email_error)
            return false
        }

        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!?.*()-_])[A-Za-z\\d@#\$%^&+=!?.*()-_]{6,}$")
        if (!regex.matches(password)) {
            _error.value = StringHelper.getString(ctx, R.string.invalid_password_error)
            return false
        }

        if (phone.length != 9 || phone.any { !it.isDigit() }
            || !(phone.startsWith("9") || phone.startsWith("2"))
        ) {
            _error.value = StringHelper.getString(ctx, R.string.invalid_phone_error)
            return false
        }

        return true
    }

    /**
     * Logs out the current user:
     * - Clears repository session
     * - Resets auth-related LiveData
     * - Clears form fields
     */
    fun logout() {
        authRepository.logout()
        _isAuthenticated.value = false
        _currentUser.value = null
        _currentShelter.value = null
        _accountType.value = AccountType.NONE
        _uiState.value = AuthUiState.Idle
        clearFields()
    }


    /**
     * Clears all registration/login form fields and resets account type to USER.
     */
    fun clearFields() {
        name.value = ""
        address.value = ""
        phone.value = ""
        email.value = ""
        password.value = ""
        accountTypeChoice.value = AccountType.USER
    }


    /**
     * Clears the current informational message.
     */
    fun clearMessage() { _message.value = null }


    /**
     * Clears the current error message.
     */
    fun clearError() { _error.value = null }


    /**
     * Resets the registration flag to false so that the UI can
     * react only once to a successful registration.
     */
    fun resetRegistration() { _isRegistered.value = false }
}
