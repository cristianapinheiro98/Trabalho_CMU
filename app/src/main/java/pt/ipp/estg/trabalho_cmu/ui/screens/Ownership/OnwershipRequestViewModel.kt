package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
//import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus
//import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository
import javax.inject.Inject

@HiltViewModel  // ← Adiciona isto!
class OwnershipViewModel @Inject constructor(
    private val repository: OwnershipRepository,
    //private val animalRepository: AnimalRepository
) : ViewModel() {

    private val _userId = MutableLiveData<String>()

    val ownerships: LiveData<List<Ownership>> =
        _userId.switchMap { id ->
            repository.getOwnershipsByUser(id)
        }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData para o animal
    //private val _animal = MutableLiveData<Animal?>()
    //val animal: LiveData<Animal?> = _animal

    fun loadOwnershipsForUser(userId: String) {
        _userId.value = userId
    }

    // Carregar detalhes do animal
    /*fun loadAnimalDetails(animalId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val animalData = animalRepository.getAnimalById(animalId)
                _animal.value = animalData
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar animal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }*/

    fun submitOwnership(request: Ownership) {
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

    fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        viewModelScope.launch {
            try {
                repository.updateOwnershipStatus(id, status)
            } catch (e: Exception) {
                _error.value = "Error updating status: ${e.message}"
            }
        }
    }

    fun deleteOwnership(ownership: Ownership) {
        viewModelScope.launch {
            try {
                repository.deleteOwnership(ownership)
            } catch (e: Exception) {
                _error.value = "Error deleting ownership: ${e.message}"
            }
        }
    }
}