package pt.ipp.estg.trabalho_cmu.ui.screens.shelter

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.repository.*
import pt.ipp.estg.trabalho_cmu.notifications.NotificationManager
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.utils.StringHelper
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong
import java.time.LocalDate

/**
 * ViewModel responsible for managing all shelter-related operations, including:
 *  - Managing adoption requests (pending, approve, reject)
 *  - Creating animals and validating animal creation forms
 *  - Uploading images and tracking upload state
 *  - Loading dynamic breed lists based on species
 *  - Synchronizing Room <-> Firebase data for users, animals, and ownerships
 *
 * The ViewModel communicates with:
 *  - AnimalRepository: for CRUD operations and live synchronization
 *  - OwnershipRepository: for approving/rejecting adoption requests
 *  - UserRepository: to ensure user information is available locally
 *  - BreedRepository: to fetch list of breeds for selected species
 *
 * This ViewModel also exposes:
 *  - UI state observable through LiveData
 *  - Loading, error, and success messages
 *  - Form validation utilities
 *  - Helpers for maintaining local UI state during image uploads
 *
 * All errors presented to the user rely on localized strings from resources.
 */
class ShelterMngViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Handles adoption/ownership request logic, including sync and request updates.
     */
    private val ownershipRepository = DatabaseModule.provideOwnershipRepository(application)

    /**
     * Manages animal CRUD operations and Firebase–Room synchronization.
     */
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)

    /**
     * Provides access to cached user profiles and allows syncing missing users.
     */
    private val userRepository = DatabaseModule.provideUserRepository(application)

    /**
     * Provides breed options based on the selected animal species.
     */
    private val breedRepository = BreedRepository()



    /**
     * Defines the current state of the shelter management UI.
     * Can represent:
     *  - Initial state
     *  - Loading operations
     *  - Success (animal created, request approved/rejected)
     *  - Error states
     */
    private val _uiState = MutableLiveData<ShelterMngUiState>(ShelterMngUiState.Initial)
    val uiState: LiveData<ShelterMngUiState> = _uiState

    /**
     * Indicates whether the ViewModel is currently performing a background job.
     */
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Holds the last error message displayed to the user.
     */
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Holds temporary success/info messages for UI feedback.
     */
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    /**
     * Contains all input fields required to create a new animal.
     * The form updates reactively based on user input.
     */
    private val _animalForm = MutableLiveData(AnimalForm())
    val animalForm: LiveData<AnimalForm> = _animalForm


    /**
     * List of breeds dynamically loaded depending on chosen species.
     */
    private val _availableBreeds = MutableLiveData<List<Breed>>(emptyList())
    val availableBreeds: LiveData<List<Breed>> = _availableBreeds

    /**
     * Indicates whether the breed list is currently loading.
     */
    private val _isLoadingBreeds = MutableLiveData(false)
    val isLoadingBreeds: LiveData<Boolean> = _isLoadingBreeds


    /**
     * Stores URL references to images uploaded for the animal listing.
     */
    private val _selectedImages = MutableLiveData<List<String>>(emptyList())
    val selectedImages: LiveData<List<String>> = _selectedImages

    /**
     * Indicates if the app is currently uploading one or more images.
     */
    private val _isUploadingImages = MutableLiveData(false)
    val isUploadingImages: LiveData<Boolean> = _isUploadingImages

    val ctx = getApplication<Application>()


    /**
     * Updates the uploading state for image operations.
     */
    fun setUploadingImages(value: Boolean) { _isUploadingImages.value = value }

    /**
     * Adds a new uploaded image URL and marks uploading as complete.
     */
    fun addImageUrl(url: String) {
        _selectedImages.value = (_selectedImages.value ?: emptyList()) + url
        _isUploadingImages.value = false
    }

    /**
     * Clears all selected images.
     */
    fun clearImages() { _selectedImages.value = emptyList() }



    /**
     * Stores pending adoption requests for the logged-in shelter.
     */
    private val _requests = MutableLiveData<List<AdoptionRequest>>()
    val requests: LiveData<List<AdoptionRequest>> = _requests

    /**
     * The Firebase UID of the currently authenticated shelter.
     */
    private val _currentShelterId = MutableLiveData<String?>()

    private val TAG = "ShelterMngViewModel"

    private suspend fun syncAllData(shelterId: String) {
        try {
            userRepository.syncUsers()
            Log.d(TAG, "Users sincronizados")

            animalRepository.syncAnimals()
            Log.d(TAG, "Animals sincronizados")

            ownershipRepository.syncPendingOwnerships(shelterId)
            Log.d(TAG, "Ownerships sincronizados")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sincronizar dados", e)
            throw e
        }
    }

    /**
     * Sets the shelter ID and automatically loads its pending adoption requests.
     */
    fun setShelterFirebaseUid(shelterId: String) {
        if (_currentShelterId.value == shelterId) return
        _currentShelterId.value = shelterId
        loadRequests(shelterId)
    }

    /**
     * Loads adoption requests, first attempting online sync.
     * If sync fails, cached Room data is used.
     */
    private fun loadRequests(shelterId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                syncAllData(shelterId)

                val ownerships = ownershipRepository.getPendingOwnershipsByShelterList(shelterId)
                _requests.value = convertList(ownerships)

            } catch (e: Exception) {

                try {
                    val cachedOwnerships =
                        ownershipRepository.getPendingOwnershipsByShelterList(shelterId)
                    _requests.value = convertList(cachedOwnerships)
                } catch (_: Exception) {
                    _requests.value = emptyList()
                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Converts Ownership entities into UI-level AdoptionRequest models.
     */
    private suspend fun convertList(list: List<Ownership>): List<AdoptionRequest> {
        return list.map { convertToAdoptionRequest(it) }
    }

    /**
     * Converts a single Ownership record into an AdoptionRequest model.
     * Ensures user and animal information is available locally.
     */
    private suspend fun convertToAdoptionRequest(ownership: Ownership): AdoptionRequest {
        val user = userRepository.getUserById(ownership.userId)
        val animal = animalRepository.getAnimalById(ownership.animalId)

        if (user == null) {
            try { userRepository.syncSpecificUser(ownership.userId) } catch (_: Exception) {}
        }

        return AdoptionRequest(
            id = ownership.id,
            nome = user?.name ?: StringHelper.getString(ctx, R.string.user_unknown),
            email = user?.email ?: StringHelper.getString(ctx, R.string.user_email_unknown),
            animal = animal?.name ?: StringHelper.getString(ctx, R.string.animal_not_found)
        )
    }


    /**
     * Approves a pending adoption request.
     * Updates the animal state and triggers UI success feedback.
     */
    fun approveRequest(request: AdoptionRequest) = viewModelScope.launch {
        _isLoading.value = true
        _uiState.value = ShelterMngUiState.Loading

        try {
            val ownership = ownershipRepository.getOwnershipById(request.id)

            if (ownership != null) {
                ownershipRepository.approveOwnershipRequest(request.id, ownership.animalId)
                    .onSuccess {
                        NotificationManager.notifyOwnershipAccepted(
                            context = ctx,
                            animalName = request.animal,
                            animalId = ownership.animalId
                        )
                        _uiState.value = ShelterMngUiState.RequestApproved
                        _message.value = StringHelper.getString( ctx, R.string.request_approved)
                        val shelterId = _currentShelterId.value
                        if (shelterId != null) {
                            syncAllData(shelterId)

                            val updatedOwnerships =
                                ownershipRepository.getPendingOwnershipsByShelterList(shelterId)
                            _requests.value = convertList(updatedOwnerships)
                        }
                    }
                    .onFailure {
                        _uiState.value = ShelterMngUiState.Error(it.message ?: "Error")
                        _error.value = it.message
                    }
            } else {
                _error.value = StringHelper.getString(ctx, R.string.error_request_not_found)
            }

        } catch (e: Exception) {
            _error.value = StringHelper.getString(ctx, R.string.error_generic)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Rejects an adoption request and updates animal/adoption state.
     */
    fun rejectRequest(request: AdoptionRequest) = viewModelScope.launch {
        _isLoading.value = true
        _uiState.value = ShelterMngUiState.Loading

        ownershipRepository.rejectOwnershipRequest(request.id)
            .onSuccess {
                NotificationManager.notifyOwnershipRejected(
                    context = ctx,
                    animalName = request.animal
                )
                _uiState.value = ShelterMngUiState.RequestRejected
                _message.value = StringHelper.getString(ctx, R.string.request_rejected)
                val shelterId = _currentShelterId.value
                if (shelterId != null) {
                    syncAllData(shelterId)


                    val updatedOwnerships =
                        ownershipRepository.getPendingOwnershipsByShelterList(shelterId)
                    _requests.value = convertList(updatedOwnerships)
                }
            }
            .onFailure {
                _uiState.value = ShelterMngUiState.Error(it.message ?: StringHelper.getString(ctx, R.string.error_generic))
                _error.value = it.message
            }

        _isLoading.value = false
    }


    /**
     * Loads available breeds for the selected species.
     */
    private fun loadBreedsBySpecies(species: String) {
        if (species.isBlank()) { _availableBreeds.value = emptyList(); return }

        _isLoadingBreeds.value = true

        breedRepository.getBreedsBySpecies(
            species,
            onSuccess = {
                _availableBreeds.postValue(it)
                _isLoadingBreeds.postValue(false)
            },
            onError = {
                _error.postValue(it)
                _availableBreeds.postValue(emptyList())
                _isLoadingBreeds.postValue(false)
            }
        )
    }

    /** Updates individual form fields reactively */
    fun onNameChange(v: String) = updateForm { copy(name = v) }
    fun onBreedChange(v: String) = updateForm { copy(breed = v) }
    fun onSpeciesChange(v: String) { updateForm { copy(species = v) }; loadBreedsBySpecies(v) }
    fun onSizeChange(v: String) = updateForm { copy(size = v) }
    fun onBirthDateChange(v: String) = updateForm { copy(birthDate = v) }
    fun onDescriptionChange(v: String) = updateForm { copy(description = v) }

    /**
     * Helper to update the form object while preserving immutability.
     */
    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }


    /**
     * Validates a birth date string in format dd/MM/yyyy.
     *
     * @return null if valid; otherwise returns localized error message.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun validateBirthDate(birthDate: String): String? {

        if (birthDate.isBlank()) return StringHelper.getString(ctx, R.string.error_birthdate_required)

        val parts = birthDate.split("/")
        if (parts.size != 3) return StringHelper.getString(ctx, R.string.error_birthdate_format)

        val day = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val year = parts[2].toIntOrNull()

        if (day !in 1..31) return StringHelper.getString(ctx, R.string.error_invalid_day)
        if (month !in 1..12) return StringHelper.getString(ctx, R.string.error_invalid_month)
        if (day == null || month == null || year == null)
            return StringHelper.getString(ctx, R.string.error_invalid_date_numbers)

        return try {
            val parsed = LocalDate.of(year, month, day)
            if (parsed.isAfter(LocalDate.now()))
                StringHelper.getString(ctx, R.string.error_future_date)
            else null
        } catch (e: Exception) {
            StringHelper.getString(ctx, R.string.error_invalid_date)
        }
    }


    /**
     * Validates all animal form fields and attempts to create a new animal.
     * If creation succeeds:
     *  - The animal is synced to Room
     *  - The form is reset
     *  - Selected images are cleared
     *  - A success message is shown
     *
     * If creation fails, a localized error is shown to the user.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAnimal() {
        val form = _animalForm.value ?: return
        val shelterId = _currentShelterId.value
            ?: run { _error.value = StringHelper.getString(ctx, R.string.error_no_shelter_logged); return }


        if (form.name.isBlank()) { _error.value = StringHelper.getString(ctx, R.string.error_name_required); return }
        if (form.breed.isBlank()) { _error.value = StringHelper.getString(ctx, R.string.error_breed_required); return }
        if (form.size.isBlank()) { _error.value = StringHelper.getString(ctx, R.string.error_size_required); return }
        if (form.species.isBlank()) { _error.value = StringHelper.getString(ctx, R.string.error_species_required); return }

        val birthError = validateBirthDate(form.birthDate)
        if (birthError != null) { _error.value = birthError; return }

        val images = _selectedImages.value ?: emptyList()
        if (images.isEmpty()) {
            _error.value = StringHelper.getString(ctx, R.string.error_add_image)
            return
        }

        val birthDateLong = dateStringToLong(form.birthDate.trim())

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ShelterMngUiState.Loading

            try {
                val animal = Animal(
                    id = "",
                    name = form.name,
                    breed = form.breed,
                    species = form.species,
                    size = form.size,
                    birthDate = birthDateLong,
                    description = form.description,
                    imageUrls = images,
                    shelterId = shelterId
                )

                animalRepository.createAnimal(animal)
                    .onSuccess {
                        animalRepository.syncAnimals()
                        NotificationManager.notifyNewAnimal(
                            context = ctx,
                            animalName = animal.name,
                            animalId = animal.id
                        )
                        _uiState.value = ShelterMngUiState.AnimalCreated(it)
                        _animalForm.value = AnimalForm()
                        clearImages()
                        _message.value = StringHelper.getString(ctx,R.string.success_animal_created)
                    }
                    .onFailure { e ->
                        _uiState.value = ShelterMngUiState.Error(e.message ?: ctx.getString(R.string.error_generic))
                        _error.value =
                            StringHelper.getString(ctx, R.string.error_save_animal) + " ${e.message}"
                    }

            } catch (e: Exception) {
                _error.value = StringHelper.getString(ctx, R.string.error_save_animal) + " ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    /** Clears last shown message */
    fun clearMessage() { _message.value = null }

    /** Clears last error */
    fun clearError() { _error.value = null }

    /** Resets view state */
    fun resetState() { _uiState.value = ShelterMngUiState.Initial }
}
