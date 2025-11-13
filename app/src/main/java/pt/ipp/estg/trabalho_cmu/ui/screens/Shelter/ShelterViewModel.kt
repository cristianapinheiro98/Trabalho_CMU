package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository

open class ShelterViewModel(  private val repository: ShelterRepository? = null
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _selectedShelter = MutableLiveData<Shelter?>()
    open val selectedShelter: LiveData<Shelter?> = _selectedShelter

    private val _shelters = MutableLiveData<List<Shelter>>(emptyList())
    val shelters: LiveData<List<Shelter>> = _shelters


    open fun loadShelterById(shelterId: Int) = viewModelScope.launch {
        try {
            _isLoading.value = true

            val shelter = repository?.getShelterById(shelterId)

            if (shelter != null) {
                _selectedShelter.value = shelter
                _error.value = null
            } else {
                _error.value = "Abrigo não encontrado"
                _selectedShelter.value = null
            }
        } catch (e: Exception) {
            _error.value = "Erro ao carregar abrigo: ${e.message}"
            _selectedShelter.value = null
        } finally {
            _isLoading.value = false
        }
    }


    fun loadAllShelters() = viewModelScope.launch {
        try {
            _isLoading.value = true

            val allShelters = repository?.getAllSheltersList()
            _shelters.value = allShelters!!
            _error.value = null

        } catch (e: Exception) {
            _error.value = "Erro ao carregar abrigos: ${e.message}"
            _shelters.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun addShelter(shelter: Shelter) = viewModelScope.launch {
        try {
            _isLoading.value = true

            repository?.insertShelter(shelter)
            _message.value = "Abrigo adicionado com sucesso!"
            _error.value = null

            loadAllShelters()

        } catch (e: Exception) {
            _error.value = "Erro ao adicionar abrigo: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }


    fun updateShelter(shelter: Shelter) = viewModelScope.launch {
        try {
            _isLoading.value = true

            repository?.updateShelter(shelter)
            _message.value = "Abrigo atualizado com sucesso!"
            _error.value = null

            if (_selectedShelter.value?.id == shelter.id) {
                _selectedShelter.value = shelter
            }

        } catch (e: Exception) {
            _error.value = "Erro ao atualizar abrigo: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteShelter(shelter: Shelter) = viewModelScope.launch {
        try {
            _isLoading.value = true

            repository?.deleteShelter(shelter)
            _message.value = "Abrigo removido com sucesso!"
            _error.value = null

            // Limpa o shelter selecionado se for o mesmo
            if (_selectedShelter.value?.id == shelter.id) {
                _selectedShelter.value = null
            }
            loadAllShelters()

        } catch (e: Exception) {
            _error.value = "Erro ao remover abrigo: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }


    fun searchSheltersByName(name: String) = viewModelScope.launch {
        try {
            _isLoading.value = true

            val results = repository?.searchSheltersByName(name)
            _shelters.value = results!!
            _error.value = null

        } catch (e: Exception) {
            _error.value = "Erro ao pesquisar abrigos: ${e.message}"
            _shelters.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
    fun clearSelectedShelter() { _selectedShelter.value = null }
}