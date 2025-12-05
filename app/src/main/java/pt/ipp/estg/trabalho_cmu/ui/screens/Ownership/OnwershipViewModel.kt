package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule

class OwnershipViewModel(application: Application) : AndroidViewModel(application) {

    private val ownershipRepository = DatabaseModule.provideOwnershipRepository(application)
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)

    // --- UI STATE ---
    private val _uiState = MutableLiveData<OwnershipUiState>(OwnershipUiState.Initial)
    val uiState: LiveData<OwnershipUiState> = _uiState

    // --- DADOS DO ANIMAL (Necessário para saber o Shelter ID) ---
    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    // --- CARREGAR ANIMAL ---
    fun loadAnimal(animalId: String) = viewModelScope.launch {
        // Não metemos loading aqui para não bloquear o ecrã todo se for rápido
        val animalData = animalRepository.getAnimalById(animalId)
        _animal.value = animalData
    }

    // --- SUBMETER PEDIDO ---
    fun submitOwnership(ownership: Ownership) = viewModelScope.launch {
        _uiState.value = OwnershipUiState.Loading

        // O repositório agora trata da verificação online e inserção no Firebase
        ownershipRepository.createOwnership(ownership)
            .onSuccess { createdOwnership ->
                _uiState.value = OwnershipUiState.OwnershipCreated(createdOwnership)
            }
            .onFailure { exception ->
                _uiState.value = OwnershipUiState.Error(exception.message ?: "Erro desconhecido ao criar pedido.")
            }
    }

    // --- RESET ---
    fun resetState() {
        _uiState.value = OwnershipUiState.Initial
    }
}