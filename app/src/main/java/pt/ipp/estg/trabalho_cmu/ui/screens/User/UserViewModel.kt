package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.repository.UserRepository

class UserViewModel(application: Application) : AndroidViewModel(application) {

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

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun loadUserByFirebaseUid(firebaseUid: String) = viewModelScope.launch {
        try {
            _isLoading.value = true

            val userFromDb = userRepository.getUserByFirebaseUid(firebaseUid)

            if (userFromDb != null) {
                _user.value = userFromDb
                _message.value = "User loaded successfully!"
                _error.value = null
            } else {
                _error.value = "User not found"
                _user.value = null
            }
        } catch (e: Exception) {
            _error.value = "Error loading user: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun loadUserByEmail(email: String) = viewModelScope.launch {
        try {
            _isLoading.value = true

            val userFromDb = userRepository.getUserByEmail(email)

            if (userFromDb != null) {
                _user.value = userFromDb
                _message.value = "User loaded successfully!"
                _error.value = null
            } else {
                _error.value = "User not found"
                _user.value = null
            }
        } catch (e: Exception) {
            _error.value = "Error loading user: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun updateUser(user: User) = viewModelScope.launch {
        try {
            _isLoading.value = true

            userRepository.updateUser(user)
            _user.value = user
            _message.value = "Profile updated successfully!"
            _error.value = null

        } catch (e: Exception) {
            _error.value = "Error updating: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}