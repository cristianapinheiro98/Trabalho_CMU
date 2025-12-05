package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule

class ShelterViewModel(application: Application) : AndroidViewModel(application) {

    private val shelterRepository = DatabaseModule.provideShelterRepository(application)

    // --- UI STATE (LiveData) ---
    private val _uiState = MutableLiveData<ShelterUiState>(ShelterUiState.Initial)
    val uiState: LiveData<ShelterUiState> = _uiState

    // LiveData do Room (Fonte de verdade da lista)
    // A UI deve observar esta variável para ver a lista de abrigos
    val shelters: LiveData<List<Shelter>> = shelterRepository.getAllShelters()

    private val _selectedShelter = MutableLiveData<Shelter?>()
    val selectedShelter: LiveData<Shelter?> = _selectedShelter

    // ========== LOAD SHELTER BY ID ==========
    fun loadShelterById(shelterId: String) = viewModelScope.launch {
        _uiState.value = ShelterUiState.Loading
        try {
            val shelter = shelterRepository.getShelterById(shelterId)
            if (shelter != null) {
                _selectedShelter.value = shelter
                _uiState.value = ShelterUiState.Success
            } else {
                _uiState.value = ShelterUiState.Error("Abrigo não encontrado")
                _selectedShelter.value = null
            }
        } catch (e: Exception) {
            _uiState.value = ShelterUiState.Error("Erro: ${e.message}")
            _selectedShelter.value = null
        }
    }

    // ========== SYNC SHELTERS ==========
    // Esta função força a atualização dos dados da Internet para o Room
    fun loadAllShelters() = viewModelScope.launch {
        _uiState.value = ShelterUiState.Loading
        try {
            shelterRepository.syncShelters() // Online First Sync -> Atualiza o Room
            _uiState.value = ShelterUiState.Success
        } catch (e: Exception) {
            _uiState.value = ShelterUiState.Error("Erro: ${e.message}")
        }
    }


    fun clearSelectedShelter() {
        _selectedShelter.value = null
    }

    fun resetState() {
        _uiState.value = ShelterUiState.Initial
    }
}