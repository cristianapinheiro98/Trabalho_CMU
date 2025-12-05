package pt.ipp.estg.trabalho_cmu.ui.screens.User

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule

class MainOptionsViewModel(application: Application) : AndroidViewModel(application) {

    // Repositórios
    private val ownershipRepository = DatabaseModule.provideOwnershipRepository(application)
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)

    // --- UI STATE ---
    private val _uiState = MutableLiveData<MainOptionsUiState>(MainOptionsUiState.Initial)
    val uiState: LiveData<MainOptionsUiState> = _uiState

    // --- DADOS DASHBOARD ---
    private val _lastWalk = MutableLiveData<WalkInfo?>()
    val lastWalk: LiveData<WalkInfo?> = _lastWalk

    private val _medals = MutableLiveData<List<Medal>>(emptyList())
    val medals: LiveData<List<Medal>> = _medals

    // --- LISTA DE ANIMAIS (Para o Dialog de seleção) ---
    private val _ownedAnimals = MutableLiveData<List<Animal>>(emptyList())
    val ownedAnimals: LiveData<List<Animal>> = _ownedAnimals

    // ========== LOAD DATA ==========
    fun loadUserData(userId: String) = viewModelScope.launch {
        _uiState.value = MainOptionsUiState.Loading
        try {
            // 1. Carregar dados do dashboard (Mocks ou Reais)
            loadDashboardData(userId)

            // 2. Carregar animais do utilizador (Lógica Real)
            loadUserAnimals(userId)

            _uiState.value = MainOptionsUiState.Success
        } catch (e: Exception) {
            _uiState.value = MainOptionsUiState.Error("Erro: ${e.message}")
        }
    }

    private suspend fun loadUserAnimals(userId: String) {

        ownershipRepository.syncUserApprovedOwnerships(userId)

        val myOwnerships = ownershipRepository.getApprovedOwnershipsByUser(userId)

        val animalsList = mutableListOf<Animal>()

        for (own in myOwnerships) {
            val animal = animalRepository.getAnimalById(own.animalId)
            if (animal != null) {
                animalsList.add(animal)
            }
        }

        _ownedAnimals.value = animalsList
    }

    private fun loadDashboardData(userId: String) {
        // Mock data (Podes substituir por chamadas reais ao WalkRepository mais tarde)
        _lastWalk.value = WalkInfo("Molly", "3km", "1 hora", "5km", "19/10/2025")
        _medals.value = listOf(Medal("🥇", "Primeira caminhada"), Medal("🥇", "10km percorridos"))
    }

    fun resetState() { _uiState.value = MainOptionsUiState.Initial }
}

// Data classes auxiliares (mantidas)
data class WalkInfo(val animalName: String, val distance: String, val duration: String, val totalDistance: String, val date: String)
data class Medal(val icon: String, val title: String)