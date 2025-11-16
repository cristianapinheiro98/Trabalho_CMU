package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter


import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository

/**
 * ViewModel responsible for managing CRUD operations related to shelters.
 *
 * Features:
 * - Load one shelter by ID
 * - Load all shelters
 * - Add, update and delete shelters
 * - Search shelters by name
 *
 * It exposes observable UI state through LiveData, including:
 * - Loading indicators
 * - Error and success messages
 * - List of shelters
 * - Selected shelter
 */
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


    /**
     * Loads a shelter by its firebaseUid.
     *
     * Workflow:
     * - Sets loading state
     * - Retrieves the shelter from the repository
     * - Updates the selected shelter
     * - Emits corresponding UI error messages if the shelter does not exist
     *
     * @param firebaseUid The ID of the shelter to load.
     */
    fun loadShelterByFirebaseUid(firebaseUid: String) = viewModelScope.launch {
        val ctx = getApplication<Application>()

        try {
            _isLoading.value = true
            val shelter = repository?.getShelterByFirebaseUid(firebaseUid)

            if (shelter != null) {
                _selectedShelter.value = shelter
                _error.value = null
            } else {
                _error.value = ctx.getString(R.string.error_shelter_not_found)
                _selectedShelter.value = null
            }
        } catch (e: Exception) {
            _error.value = ctx.getString(R.string.error_loading_shelter) + " ${e.message}"
            _selectedShelter.value = null
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Loads all shelters available in the repository.
     *
     * Workflow:
     * - Sets loading state
     * - Retrieves all shelters
     * - Updates the 'shelters' LiveData with the results
     * - Emits an error message in case of failure
     */
    fun loadAllShelters() = viewModelScope.launch {
        val ctx = getApplication<Application>()

        try {
            _isLoading.value = true

            val allShelters = shelterRepository.getAllSheltersList()
            _shelters.value = allShelters
            _error.value = null

        } catch (e: Exception) {
            _error.value = ctx.getString(R.string.error_loading_shelters) + " ${e.message}"
            _shelters.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }


    /**
     * Updates an existing shelter in the database.
     *
     * Workflow:
     * - Sets loading state
     * - Calls repository to update the entity
     * - Updates the selected shelter if it was the edited one
     * - Emits a success message on completion
     * - Emits an error message in case of exception
     *
     * @param shelter The shelter entity with updated fields.
     */
    fun updateShelter(shelter: Shelter) = viewModelScope.launch {
        val ctx = getApplication<Application>()

        try {
            _isLoading.value = true

            repository?.updateShelter(shelter)
            _message.value = ctx.getString(R.string.success_shelter_updated)
            _error.value = null

            if (_selectedShelter.value?.id == shelter.id) {
                _selectedShelter.value = shelter
            }

        } catch (e: Exception) {
            _error.value = ctx.getString(R.string.error_updating_shelter) + " ${e.message}"
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

    /**
     * Clears the current success message.
     */
    fun clearMessage() { _message.value = null }

    /**
     * Clears the current error message.
     */
    fun clearError() { _error.value = null }

    /**
     * Clears the currently selected shelter.
     */
    fun clearSelectedShelter() { _selectedShelter.value = null }
}