package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter


import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
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
    val shelters: LiveData<List<Shelter>> = repository?.getAllShelters() ?: MutableLiveData(emptyList())

    fun loadShelterByFirebaseUid(firebaseUid: String) = viewModelScope.launch {
        try {
            _isLoading.value = true
            val shelter = repository?.getShelterByFirebaseUid(firebaseUid)

            if (shelter != null) {
                _selectedShelter.value = shelter
                _error.value = null
            } else {
                _error.value = "Shelter not found"
                _selectedShelter.value = null
            }
        } catch (e: Exception) {
            _error.value = "Error loading shelter: ${e.message}"
            _selectedShelter.value = null
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

//    fun searchSheltersByName(name: String) = viewModelScope.launch {
//        try {
//            _isLoading.value = true
//
//            val results = repository?.searchSheltersByName(name)
//            _shelters.value = results!!
//            _error.value = null
//
//        } catch (e: Exception) {
//            _error.value = "Erro ao pesquisar abrigos: ${e.message}"
//            _shelters.value = emptyList()
//        } finally {
//            _isLoading.value = false
//        }
//    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
    fun clearSelectedShelter() { _selectedShelter.value = null }
}