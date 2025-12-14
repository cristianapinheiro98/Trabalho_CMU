package pt.ipp.estg.trabalho_cmu.ui.screens.shelter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.utils.StringHelper

/**
 * ViewModel responsible for:
 * - Loading shelters from the repository
 * - Selecting a shelter by ID
 * - Managing UI state for shelter-related screens
 *
 * Uses AndroidViewModel so that Application context can be accessed
 * for retrieving localized string resources.
 */
class ShelterViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Application context used mainly for accessing string resources.
     */
    private val appContext = getApplication<Application>()

    /**
     * Repository that provides shelter data from Room and Firebase.
     */
    private val shelterRepository = DatabaseModule.provideShelterRepository(application)

    /**
     * Backing field for the current shelter UI state.
     */
    private val _uiState = MutableLiveData<ShelterUiState>(ShelterUiState.Initial)

    /**
     * Public observable UI state for shelter-related operations.
     */
    val uiState: LiveData<ShelterUiState> = _uiState

    /** Live list of shelters observed from Room */
    val shelters: LiveData<List<Shelter>> = shelterRepository.getAllShelters()

    /**
     * Backing field for the currently selected shelter, if any.
     */
    private val _selectedShelter = MutableLiveData<Shelter?>()

    /**
     * Public observable currently selected shelter.
     */
    val selectedShelter: LiveData<Shelter?> = _selectedShelter

    /**
     * Loads a shelter by ID from the local database.
     * If found → updates selectedShelter and UI state.
     * If not found → emits an Error state with a localized message.
     */
    fun loadShelterById(shelterId: String) = viewModelScope.launch {
        _uiState.value = ShelterUiState.Loading

        try {
            val shelter = shelterRepository.getShelterById(shelterId)

            if (shelter != null) {
                _selectedShelter.value = shelter
                _uiState.value = ShelterUiState.Success
            } else {
                _selectedShelter.value = null
                _uiState.value = ShelterUiState.Error(
                    StringHelper.getString(appContext, R.string.error_shelter_not_found)
                )
            }

        } catch (e: Exception) {
            _selectedShelter.value = null
            _uiState.value = ShelterUiState.Error(
                StringHelper.getString(appContext, R.string.error_loading_shelter) + " ${e.message}"
            )
        }
    }

    /**
     * Synchronizes shelter data with Firebase, updating the Room cache.
     * If the sync fails, emits an Error state with a localized message.
     */
    fun loadAllShelters() = viewModelScope.launch {
        _uiState.value = ShelterUiState.Loading

        try {
            shelterRepository.syncShelters()
            _uiState.value = ShelterUiState.Success

        } catch (e: Exception) {
            _uiState.value = ShelterUiState.Error(
                StringHelper.getString(appContext, R.string.error_sync_shelters) + " ${e.message}"
            )
        }
    }

    /**
     * Clears the currently selected shelter.
     */
    fun clearSelectedShelter() {
        _selectedShelter.value = null
    }

    /**
     * Resets the UI state back to Initial.
     */
    fun resetState() {
        _uiState.value = ShelterUiState.Initial
    }
}
