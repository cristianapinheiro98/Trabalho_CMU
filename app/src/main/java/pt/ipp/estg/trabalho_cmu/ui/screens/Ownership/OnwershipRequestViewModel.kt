import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.OwnershipRequest
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRequestRepository

class OwnershipRequestViewModel(
    private val repository: OwnershipRequestRepository,
    //private val animalRepository: AnimalRepository // Adicionar
) : ViewModel() {

    private val _userId = MutableLiveData<String>()

    val ownerships: LiveData<List<OwnershipRequest>> =
        _userId.switchMap { id ->
            repository.getOwnershipsByUser(id)
        }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Novo: LiveData para o animal
    /*private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal*/

    fun loadOwnershipsForUser(userId: String) {
        _userId.value = userId
    }

    // Novo: Carregar detalhes do animal
    fun loadAnimalDetails(animalId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
               // val animalData = animalRepository.getAnimalById(animalId)
                //_animal.value = animalData
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar animal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        viewModelScope.launch {
            try {
                repository.updateOwnershipStatus(id, status)
            } catch (e: Exception) {
                _error.value = "Error updating status: ${e.message}"
            }
        }
    }

    fun deleteOwnership(ownership: OwnershipRequest) {
        viewModelScope.launch {
            try {
                repository.deleteOwnership(ownership)
            } catch (e: Exception) {
                _error.value = "Error deleting ownership: ${e.message}"
            }
        }
    }

    class Factory(
        private val ownershipRepository: OwnershipRequestRepository,
        //private val animalRepository: AnimalRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OwnershipRequestViewModel::class.java)) {
                //@Suppress("UNCHECKED_CAST")
                //return OwnershipViewModel(OwnershipRequestViewModel, animalRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}