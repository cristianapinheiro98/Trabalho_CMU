package pt.ipp.estg.trabalho_cmu.ui.sc

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.OwnershipRequest
import pt.ipp.estg.trabalho_cmu.data.local.entities.OwnershipStatus
import pt.ipp.estg.trabalho_cmu.data.repositories.OwnershipRepository
import androidx.lifecycle.Transformations

/**
 * ViewModel responsible for managing ownership-related data and logic.
 * Uses LiveData so the UI automatically updates when the database changes.
 */
class OwnershipViewModel(
    private val repository: OwnershipRepository
) : ViewModel() {

    // --- LiveData for the current logged user ID ---
    private val _userId = MutableLiveData<String>()

    /**
     * LiveData list of ownerships automatically updated
     * when _userId changes (thanks to switchMap).
     */
    val ownerships: LiveData<List<OwnershipRequest>> =
        Transformations.switchMap(_userId) { id ->
            repository.getOwnershipsByUser(id)
        }

    // --- Loading and error states for UI feedback ---
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    /**
     * Loads ownership requests for a specific user.
     * Changing the userId automatically triggers a new DB query.
     */
    fun loadOwnershipsForUser(userId: String) {
        _userId.value = userId
    }

    /**
     * Inserts a new ownership (special adoption) into the database.
     */
    fun submitOwnership(request: OwnershipRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addOwnership(request)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error adding ownership: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the status of a request (e.g., approved, rejected).
     */
    fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        viewModelScope.launch {
            try {
                repository.updateOwnershipStatus(id, status)
            } catch (e: Exception) {
                _error.value = "Error updating status: ${e.message}"
            }
        }
    }

    /**
     * Deletes an ownership record.
     */
    fun deleteOwnership(ownership: OwnershipRequest) {
        viewModelScope.launch {
            try {
                repository.deleteOwnership(ownership)
            } catch (e: Exception) {
                _error.value = "Error deleting ownership: ${e.message}"
            }
        }
    }

    // --- Factory used to create this ViewModel with arguments ---
    class Factory(private val repository: OwnershipRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OwnershipViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OwnershipViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
