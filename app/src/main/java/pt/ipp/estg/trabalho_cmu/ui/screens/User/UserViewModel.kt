package pt.ipp.estg.trabalho_cmu.ui.screens.User

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = DatabaseModule.provideUserRepository(application)

    // --- MUDANÇA: StateFlow -> LiveData ---
    private val _uiState = MutableLiveData<UserUiState>(UserUiState.Initial)
    val uiState: LiveData<UserUiState> = _uiState

    // User data
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // ========== LOAD USER BY ID ==========
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

    // ========== UPDATE USER ==========
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

    fun resetState() {
        _uiState.value = UserUiState.Initial
    }
}