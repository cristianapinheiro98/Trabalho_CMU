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
 *
 * This ViewModel uses Kotlin coroutines and runs all data operations asynchronously.
 */
open class ShelterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val shelterRepository = ShelterRepository(db.shelterDao())

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


    /**
     * Loads a shelter by its ID.
     *
     * Workflow:
     * - Sets loading state
     * - Retrieves the shelter from the repository
     * - Updates the selected shelter
     * - Emits corresponding UI error messages if the shelter does not exist
     *
     * @param shelterId The ID of the shelter to load.
     */
    open fun loadShelterById(shelterId: Int) = viewModelScope.launch {
        val ctx = getApplication<Application>()
        try {
            _isLoading.value = true

            val shelter = shelterRepository.getShelterById(shelterId)

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
     * Adds a new shelter to the database.
     *
     * Workflow:
     * - Sets loading state
     * - Inserts the shelter via the repository
     * - Emits a success message
     * - Reloads all shelters after insertion
     * - Emits an error message in case of failure
     *
     * @param shelter The shelter entity to insert.
     */
    fun addShelter(shelter: Shelter) = viewModelScope.launch {
        val ctx = getApplication<Application>()
        try {
            _isLoading.value = true

            shelterRepository.insertShelter(shelter)
            _message.value = ctx.getString(R.string.success_shelter_added)
            _error.value = null

            loadAllShelters()

        } catch (e: Exception) {
            _error.value = ctx.getString(R.string.error_adding_shelter) + " ${e.message}"
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

            shelterRepository.updateShelter(shelter)
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

    /**
     * Deletes a shelter from the database.
     *
     * Workflow:
     * - Sets loading state
     * - Deletes the shelter using the repository
     * - Clears selected shelter if it matches the removed one
     * - Reloads all shelters after deletion
     * - Emits success or error message appropriately
     *
     * @param shelter The shelter entity to remove.
     */
    fun deleteShelter(shelter: Shelter) = viewModelScope.launch {
        val ctx = getApplication<Application>()
        try {
            _isLoading.value = true

            shelterRepository.deleteShelter(shelter)
            _message.value = ctx.getString(R.string.success_shelter_removed)
            _error.value = null

            if (_selectedShelter.value?.id == shelter.id) {
                _selectedShelter.value = null
            }
            loadAllShelters()

        } catch (e: Exception) {
            _error.value = ctx.getString(R.string.error_removing_shelter) + " ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Searches shelters by name using partial string matching.
     *
     * Workflow:
     * - Sets loading state
     * - Retrieves matching shelters from repository
     * - Updates the shelters LiveData with results
     *
     * @param name The text to search shelters by.
     */
    fun searchSheltersByName(name: String) = viewModelScope.launch {
        val ctx = getApplication<Application>()
        try {
            _isLoading.value = true

            val results = shelterRepository.searchSheltersByName(name)
            _shelters.value = results
            _error.value = null

        } catch (e: Exception) {
            _error.value = ctx.getString(R.string.error_searching_shelters) + " ${e.message}"
            _shelters.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

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
