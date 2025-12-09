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
import pt.ipp.estg.trabalho_cmu.R

/**
 * ViewModel responsible for handling the creation of ownership (adoption) requests
 * and exposing the necessary UI state to the Ownership screen.
 *
 * Responsibilities:
 *  - Load local animal information based on its ID
 *  - Submit ownership/adoption requests to the OwnershipRepository
 *  - Expose UI states such as loading, success, and error
 *
 * This ViewModel interacts with:
 *  - OwnershipRepository: to create adoption requests
 *  - AnimalRepository: to load required animal data from local Room storage
 *
 * All errors are passed upward to the UI, where they can be translated
 * into localized messages defined in strings.xml.
 */
class OwnershipViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Repository that manages creation and synchronization of ownership requests.
     */
    private val ownershipRepository = DatabaseModule.provideOwnershipRepository(application)

    /**
     * Repository for retrieving animal details, stored locally in Room.
     */
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)

    /**
     * Represents current UI state (Initial, Loading, Success, Error).
     */
    private val _uiState = MutableLiveData<OwnershipUiState>(OwnershipUiState.Initial)
    val uiState: LiveData<OwnershipUiState> = _uiState

    /**
     * Holds the animal being adopted, retrieved via its ID.
     */
    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    val ctx = getApplication<Application>()

    /**
     * Loads the animal associated with a given ID from Room database.
     * This ensures the UI has context for the ownership request.
     */
    fun loadAnimal(animalId: String) = viewModelScope.launch {
        val animalData = animalRepository.getAnimalById(animalId)
        _animal.value = animalData
    }

    /**
     * Submits an ownership (adoption) request.
     *
     * @param ownership The ownership request object containing userId, animalId, etc.
     *
     * Updates UI state:
     *  - Loading → when request is being submitted
     *  - OwnershipCreated → on success
     *  - Error → on failure with a message
     */
    fun submitOwnership(ownership: Ownership) = viewModelScope.launch {
        _uiState.value = OwnershipUiState.Loading

        ownershipRepository.createOwnership(ownership)
            .onSuccess { createdOwnership ->
                _uiState.value = OwnershipUiState.OwnershipCreated(createdOwnership)
            }
            .onFailure { exception ->
                _uiState.value = OwnershipUiState.Error(
                    exception.message ?: ctx.getString(R.string.error_submit_ownership)
                )
            }
    }

    /**
     * Resets the UI state back to Initial.
     * Useful when leaving and re-entering the screen.
     */
    fun resetState() {
        _uiState.value = OwnershipUiState.Initial
    }
}
