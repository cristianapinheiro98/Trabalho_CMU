package pt.ipp.estg.trabalho_cmu.ui.screens.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule

/**
 * ViewModel responsible for managing user profile data.
 *
 * Handles operations related to fetching and updating user information,
 * exposing the current state via [uiState] and the user data via [user].
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = DatabaseModule.provideUserRepository(application)
    private val _uiState = MutableLiveData<UserUiState>(UserUiState.Initial)
    val uiState: LiveData<UserUiState> = _uiState

    // User data
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    /**
     * Asynchronously loads a user by their unique identifier.
     *
     * Updates [uiState] to Loading, then Success (if found) or Error.
     * If successful, the [user] LiveData is updated with the fetched data.
     *
     * @param userId The unique identifier of the user to load.
     */
    fun loadUserById(userId: String) = viewModelScope.launch {
        _uiState.value = UserUiState.Loading
        try {
            val userFromDb = userRepository.getUserById(userId)
            if (userFromDb != null) {
                _user.value = userFromDb
                _uiState.value = UserUiState.Success
            } else {
                _uiState.value = UserUiState.Error("Utilizador não encontrado")
            }
        } catch (e: Exception) {
            _uiState.value = UserUiState.Error("Erro: ${e.message}")
        }
    }

    /**
     * Updates the user information in the repository.
     *
     * Updates the local [user] LiveData immediately and sets [uiState] to UserUpdated
     * upon success. Handles exceptions by setting the Error state.
     *
     * @param user The modified user entity to be saved.
     */
    fun updateUser(user: User) = viewModelScope.launch {
        _uiState.value = UserUiState.Loading
        try {
            userRepository.updateUser(user)
            _user.value = user // Atualiza localmente
            _uiState.value = UserUiState.UserUpdated
        } catch (e: Exception) {
            _uiState.value = UserUiState.Error("Erro ao atualizar: ${e.message}")
        }
    }

    /**
     * Resets the UI state to Initial.
     *
     * after they have been consumed by the UI.
     */
    fun resetState() {
        _uiState.value = UserUiState.Initial
    }
}