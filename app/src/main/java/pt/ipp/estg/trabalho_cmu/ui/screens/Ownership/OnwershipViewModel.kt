package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository

class OwnershipViewModel(application: Application) : AndroidViewModel(application) {

    private val ownershipRepository: OwnershipRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        OwnershipRepository(
            db.ownershipDao(),
            FirebaseFirestore.getInstance()  // ← Passa o Firestore!
        )
    }

    private val animalRepository: AnimalRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        AnimalRepository(db.animalDao(), FirebaseFirestore.getInstance())
    }

    // ========= USER OWNERSHIPS ========= //
    private val _userId = MutableLiveData<Int>()

    val ownerships: LiveData<List<Ownership>> =
        _userId.switchMap { id ->
            ownershipRepository.getOwnershipsByUser(id)
        }

    fun loadOwnershipsForUser(userId: Int) {
        _userId.value = userId
        // Busca do Firebase ao carregar
        viewModelScope.launch {
            ownershipRepository.fetchOwnerships()
        }
    }

    // ========= UI STATES ========= //
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // ========= ANIMAL DETAILS ========= //
    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    fun loadAnimalDetails(animalId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _animal.value = animalRepository.getAnimalById(animalId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar animal: ${e.message}"
                _animal.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========= OWNERSHIP ACTIONS ========= //
    fun submitOwnership(request: Ownership) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Usa createOwnership em vez de addOwnership
                val result = ownershipRepository.createOwnership(request)

                result.onSuccess {
                    _message.value = "Pedido de adoção submetido com sucesso!"
                    _error.value = null
                }.onFailure { e ->
                    _error.value = "Erro ao submeter pedido: ${e.message}"
                }
            } catch (e: Exception) {
                _error.value = "Erro ao submeter pedido: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        viewModelScope.launch {
            try {
                ownershipRepository.updateOwnershipStatus(id, status)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar estado: ${e.message}"
            }
        }
    }

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

    fun clearError() { _error.value = null }
    fun clearMessage() { _message.value = null }
}