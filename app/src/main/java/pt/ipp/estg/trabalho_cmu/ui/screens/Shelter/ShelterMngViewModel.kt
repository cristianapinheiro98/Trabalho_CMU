package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.repository.*
import java.time.LocalDate


/**
 * ViewModel responsible for managing all shelter-side operations in the Tailwagger application.
 *
 * This includes:
 * - Managing adoption/ownership requests
 * - Creating animals and validating form data
 * - Uploading and managing Firebase image URLs
 * - Loading breeds dynamically from an external API based on species
 * - Updating local Room database and synchronizing with Firebase Firestore
 *
 * The ViewModel uses LiveData to expose UI state such as:
 * - Loading indicators
 * - User-visible success and error messages
 * - Form field values
 * - Available breeds
 */

class ShelterMngViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val ownershipRepository = OwnershipRepository(
        db.ownershipDao(),
        FirebaseFirestore.getInstance()
    )

    private val animalRepository = AnimalRepository(
        db.animalDao(),
        FirebaseFirestore.getInstance()
    )

    private val shelterRepository = ShelterRepository(
        db.shelterDao(),
        FirebaseFirestore.getInstance()
    )
    private val userRepository = UserRepository(db.userDao())
    private val breedRepository = BreedRepository()

    // ---------------------- UI STATE ----------------------
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // ---------------------- FORM --------------------------
    private val _animalForm = MutableLiveData(AnimalForm())
    val animalForm: LiveData<AnimalForm> = _animalForm

    // ---------------------- BREEDS ------------------------
    private val _availableBreeds = MutableLiveData<List<Breed>>(emptyList())
    val availableBreeds: LiveData<List<Breed>> = _availableBreeds

    private val _isLoadingBreeds = MutableLiveData(false)
    val isLoadingBreeds: LiveData<Boolean> = _isLoadingBreeds

    // ---------------------- IMAGES ------------------------
    private val _selectedImages = MutableLiveData<List<String>>(emptyList())
    val selectedImages: LiveData<List<String>> = _selectedImages

    private val _isUploadingImages = MutableLiveData(false)
    val isUploadingImages: LiveData<Boolean> = _isUploadingImages

    /**
     * Sets whether image uploading to Firebase Storage is in progress.
     *
     * @param value True when upload is ongoing, false otherwise.
     */
    fun setUploadingImages(value: Boolean) {
        _isUploadingImages.value = value
    }

    /**
     * Adds a Firebase Storage URL to the current list of uploaded images.
     * This is called once an upload operation successfully returns a URL.
     *
     * @param url The public image URL returned from Firebase Storage.
     */
    fun addImageUrl(url: String) {
        _selectedImages.value = _selectedImages.value!! + url
        _isUploadingImages.value = false
    }

    /**
     * Clears all previously uploaded Firebase image URLs.
     */
    fun clearImages() {
        _selectedImages.value = emptyList()
    }

    // ---------------------- SHELTER FIREBASE UID ---------------------
    private val _currentShelterFirebaseUid = MutableLiveData<String?>(null)
    val currentShelterFirebaseUid: LiveData<String?> = _currentShelterFirebaseUid


    /**
     * Sets the active shelter firebaseUid for the session.
     *
     * Once the ID is set, this method triggers:
     * - Fetching all ownership records from Firebase
     * - Automatic filtering of adoption requests through the `requests` LiveData
     *
     * @param firebaseUid The unique identifier of the shelter currently logged in.
     */
    fun setShelterFirebaseUid(firebaseUid: String) {
        _currentShelterFirebaseUid.value = firebaseUid

        viewModelScope.launch {
            try {
                ownershipRepository.fetchOwnerships()
            } catch (e: Exception) {
                println("Error getting ownerships: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // ---------------------- ADOPTION REQUESTS ---------------
    val requests: LiveData<List<AdoptionRequest>> =
        _currentShelterFirebaseUid.switchMap { shelterFirebaseUid ->
            if (shelterFirebaseUid != null) {
                MediatorLiveData<List<AdoptionRequest>>().apply {
                    addSource(
                        ownershipRepository.getOwnershipsByShelter(shelterFirebaseUid)
                    ) { ownerships ->
                        viewModelScope.launch {
                            value = try {
                                ownerships.mapNotNull { convertToAdoptionRequest(it) }
                            } catch (e: Exception) {
                                println("Error converting ownership: ${e.message}")
                                emptyList()
                            }
                        }
                    }
                }
            } else {
                MutableLiveData(emptyList())
            }
        }


    /**
     * Converts an Ownership entity into an AdoptionRequest model suitable for the UI layer.
     * This method fetches the associated User and Animal records before assembling the final model.
     *
     * @param ownership The ownership record retrieved from the database.
     * @return An AdoptionRequest object.
     */
    private suspend fun convertToAdoptionRequest(ownership: Ownership): AdoptionRequest {
        val user = userRepository.getUserByFirebaseUid(ownership.userFirebaseUid)
        val animal = animalRepository.getAnimalByFirebaseUid(ownership.animalFirebaseUid)

        return AdoptionRequest(
            id = ownership.id.toString(),
            nome = user?.name ?: "Unknown",
            email = user?.email ?: "N/A",
            animal = animal?.name ?: "Animal #${ownership.animalFirebaseUid}"
        )
    }

    // ---------------------- APPROVE / REJECT OWNERSHIP REQUESTS --------------

    /**
     * Approves an adoption/ownership request.
     *
     * @param request The adoption request selected by the shelter.
     */
    fun approveRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val ownershipId = request.id.toIntOrNull() ?: return@launch
                ownershipRepository.approveOwnershipRequest(ownershipId)

                val ownership = ownershipRepository.getOwnershipById(ownershipId)
                val animalFirebaseUid = ownership?.animalFirebaseUid ?: return@launch

                val animal = animalRepository.getAnimalByFirebaseUid(animalFirebaseUid)
                val animalId = animal?.id ?: return@launch

                animalRepository.changeAnimalStatusToOwned(animalId)

                _message.value = getApplication<Application>().getString(R.string.success_request_approved)
                _error.value = null

            } catch (e: Exception) {
                _error.value =
                    getApplication<Application>().getString(R.string.error_approve_request) + " ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Rejects an adoption/ownership request.
     *
     * @param request The adoption request to reject.
     */
    fun rejectRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val ownershipId = request.id.toIntOrNull() ?: return@launch
                ownershipRepository.rejectOwnershipRequest(ownershipId)

                _message.value = getApplication<Application>().getString(R.string.success_request_rejected)
                _error.value = null

            } catch (e: Exception) {
                _error.value =
                    getApplication<Application>().getString(R.string.error_reject_request) + " ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------- BREEDS LOADER -------------------

    /**
     * Loads breeds based on the provided species.
     */
    private fun loadBreedsBySpecies(species: String) {
        if (species.isBlank()) {
            _availableBreeds.value = emptyList()
            return
        }

        _isLoadingBreeds.value = true

        breedRepository.getBreedsBySpecies(
            species,
            onSuccess = {
                _availableBreeds.value = it
                _isLoadingBreeds.value = false
            },
            onError = { error ->
                _error.value = error
                _availableBreeds.value = emptyList()
                _isLoadingBreeds.value = false
            }
        )
    }

    // ---------------------- FORM HANDLERS -------------------
    /**
     * Updates the 'name' field in the animal creation form.
     * @param value The entered name.
     */
    fun onNameChange(v: String) = updateForm { copy(name = v) }

    /**
     * Updates the 'breed' field in the animal creation form.
     * @param value The selected breed.
     */
    fun onBreedChange(v: String) = updateForm { copy(breed = v) }

    /**
     * Updates the 'species' field in the animal creation form.
     *
     * This also triggers dynamic breed loading via the BreedRepository.
     *
     * @param value The selected species.
     */
    fun onSpeciesChange(v: String) {
        updateForm { copy(species = v) }
        loadBreedsBySpecies(v)
    }

    /**
     * Updates the 'size' field in the animal creation form.
     * @param value The selected size.
     */
    fun onSizeChange(v: String) = updateForm { copy(size = v) }


    /**
     * Updates the birthdate field in the animal creation form.
     * @param value A string in DD/MM/YYYY format.
     */
    fun onBirthDateChange(v: String) = updateForm { copy(birthDate = v) }

    /**
     * Updates the 'description' field in the animal form.
     * @param value The entered description.
     */
    fun onDescriptionChange(v: String) = updateForm { copy(description = v) }

    /**
     * Internal utility method that updates one or more fields in the AnimalForm.
     *
     * This method keeps the rest of the form unchanged by copying the existing
     * fields and applying only the modified fields provided in the lambda block.
     */
    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }

    // ---------------------- VALIDATE DATE -------------------

    /**
     * Validates the birthdate entered by the user.
     *
     * Validation rules:
     * - Must not be empty
     * - Must follow the DD/MM/YYYY format
     * - Must contain valid day and month values
     * - Must consist only of numeric components
     * - Must not represent a future date
     *
     * @param birthDate A string representing the date.
     * @return A user-visible error message if invalid, or null if valid.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun validateBirthDate(birthDate: String): String? {
        val ctx = getApplication<Application>()

        if (birthDate.isBlank())
            return ctx.getString(R.string.error_birthdate_required)

        val parts = birthDate.split("/")
        if (parts.size != 3)
            return ctx.getString(R.string.error_birthdate_format)

        val day = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val year = parts[2].toIntOrNull()

        if (day !in 1..31)
            return ctx.getString(R.string.error_invalid_day)

        if (month !in 1..12)
            return ctx.getString(R.string.error_invalid_month)

        if (day == null || month == null || year == null)
            return ctx.getString(R.string.error_invalid_date_numbers)

        return try {
            val date = LocalDate.of(year, month, day)
            if (date.isAfter(LocalDate.now()))
                ctx.getString(R.string.error_future_date)
            else null
        } catch (e: Exception) {
            "Invalid date."
        }
    }

    // ---------------------- SAVE ANIMAL ---------------------

    /**
     * Validates the form and creates a new animal entry.
     *
     * Steps:
     * 1. Validates all form fields (name, species, breed, size, birthdate).
     * 2. Ensures the user uploaded at least one image.
     * 3. Constructs an Animal object with the provided data.
     * 4. Saves the Animal to Firebase Firestore and Room using AnimalRepository.
     * 5. Clears the form and image selection on success.
     * 6. Emits an appropriate success or error message to the UI.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAnimal() {
        val form = _animalForm.value ?: AnimalForm()
        val ctx = getApplication<Application>()

        if (form.name.isBlank()) { _error.value = ctx.getString(R.string.error_name_required); return }
        if (form.breed.isBlank()) { _error.value = ctx.getString(R.string.error_breed_required); return }
        if (form.size.isBlank()) { _error.value = ctx.getString(R.string.error_size_required); return }
        if (form.species.isBlank()) { _error.value = ctx.getString(R.string.error_species_required); return }

        val birthError = validateBirthDate(form.birthDate)
        if (birthError != null) { _error.value = birthError; return }

        val shelterFirebaseUid = _currentShelterFirebaseUid.value ?: run {
            _error.value = ctx.getString(R.string.error_no_shelter_id)
            return
        }

        val images = selectedImages.value ?: emptyList()
        if (images.isEmpty()) {
            _error.value = ctx.getString(R.string.error_add_image)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newAnimal = Animal(
                    name = form.name.trim(),
                    breed = form.breed.trim(),
                    species = form.species.trim(),
                    size = form.size.trim(),
                    birthDate = form.birthDate.trim(),
                    description = form.description.trim(),
                    imageUrls = images,
                    shelterFirebaseUid = shelterFirebaseUid
                )

                val result = animalRepository.createAnimal(newAnimal)

                result.onSuccess {
                    _animalForm.value = AnimalForm()
                    clearImages()
                    _availableBreeds.value = emptyList()
                    _message.value = ctx.getString(R.string.success_animal_created)
                }

                result.onFailure { e ->
                    _error.value = ctx.getString(R.string.error_save_animal) + " ${e.message}"
                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------- UTILS ---------------------------

    /**
     * Clears the currently displayed success message.
     */
    fun clearMessage() { _message.value = null }

    /**
     * Clears the currently displayed error message.
     */
    fun clearError() { _error.value = null }
}