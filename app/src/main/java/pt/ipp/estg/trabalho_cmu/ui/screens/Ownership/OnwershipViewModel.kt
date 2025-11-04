package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus
//import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository

class OwnershipViewModel(application: Application) : AndroidViewModel(application) {

    private val ownershipRepository: OwnershipRepository
    private val animalRepository: AnimalRepository

    init {
        val database = AppDatabase.getDatabase(application)
        ownershipRepository = OwnershipRepository(database.ownershipDao())
        animalRepository = AnimalRepository(database.animalDao())
    }

    // User ID for filtering ownerships
    private val _userId = MutableLiveData<String>()

    /**
     * LiveData that automatically updates when ownerships change in database.
     * Filtered by user ID.
     */
    val ownerships: LiveData<List<Ownership>> =
        _userId.switchMap { id ->
            ownershipRepository.getOwnershipsByUser(id)
        }

    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Animal data for ownership form screen
    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    /**
     * Set the user ID to filter ownerships.
     * This triggers the ownerships LiveData to update.
     */
    fun loadOwnershipsForUser(userId: String) {
        _userId.value = userId
    }

    /**
     * Load animal details for the ownership form screen.
     * Used when user wants to request ownership for a specific animal.
     */
    fun loadAnimalDetails(animalId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val animalData = animalRepository.getAnimalById(animalId)
                _animal.value = animalData
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar animal: ${e.message}"
                _animal.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Submit a new ownership request.
     * Shows loading state and handles errors.
     */
    fun submitOwnership(request: Ownership) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                ownershipRepository.addOwnership(request)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao submeter pedido: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update the status of an ownership request.
     * Used by admins to approve/reject requests.
     */
    fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        viewModelScope.launch {
            try {
                ownershipRepository.updateOwnershipStatus(id, status)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar status: ${e.message}"
            }
        }
    }

    /**
     * Delete an ownership request.
     */
    fun deleteOwnership(ownership: Ownership) {
        viewModelScope.launch {
            try {
                ownershipRepository.deleteOwnership(ownership)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao eliminar pedido: ${e.message}"
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
}