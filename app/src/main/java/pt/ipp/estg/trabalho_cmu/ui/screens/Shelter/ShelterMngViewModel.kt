package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.repository.*
import java.time.LocalDate

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

    // ---------------------- IMAGES (FIREBASE URLS) ------------------------
    private val _selectedImages = MutableLiveData<List<String>>(emptyList())
    val selectedImages: LiveData<List<String>> = _selectedImages

    private val _isUploadingImages = MutableLiveData(false)
    val isUploadingImages: LiveData<Boolean> = _isUploadingImages

    fun setUploadingImages(value: Boolean) {
        _isUploadingImages.value = value
    }

    fun addImageUrl(url: String) {
        _selectedImages.value = _selectedImages.value!! + url
        _isUploadingImages.value = false
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
    }

    // ---------------------- SHELTER FIREBASE UID ---------------------
    private val _currentShelterFirebaseUid = MutableLiveData<String?>(null)
    val currentShelterFirebaseUid: LiveData<String?> = _currentShelterFirebaseUid

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

    // ---------------------- ADOPTION REQUEST ---------------
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

                _message.value = "Request approved successfully!"
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Error approving request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val ownershipId = request.id.toIntOrNull() ?: return@launch
                ownershipRepository.rejectOwnershipRequest(ownershipId)

                _message.value = "Request rejected"
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Error rejecting request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------- BREEDS LOADER -------------------
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
            onError = {
                _error.value = it
                _availableBreeds.value = emptyList()
                _isLoadingBreeds.value = false
            }
        )
    }

    // ---------------------- FORM HANDLERS -------------------
    fun onNameChange(value: String) = updateForm { copy(name = value) }
    fun onBreedChange(value: String) = updateForm { copy(breed = value) }

    fun onSpeciesChange(value: String) {
        updateForm { copy(species = value) }
        loadBreedsBySpecies(value)
    }

    fun onSizeChange(value: String) = updateForm { copy(size = value) }
    fun onBirthDateChange(value: String) = updateForm { copy(birthDate = value) }
    fun onDescriptionChange(value: String) = updateForm { copy(description = value) }

    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }

    // ---------------------- VALIDATE DATE -------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun validateBirthDate(birthDate: String): String? {
        if (birthDate.isBlank()) return "Birth date is required."

        val parts = birthDate.split("/")
        if (parts.size != 3) return "Date must be in DD/MM/YYYY format."

        val day = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val year = parts[2].toIntOrNull()

        if (day !in 1..31) return "Invalid day."
        if (month !in 1..12) return "Invalid month."
        if (day == null || month == null || year == null) return "Day, month and year must be numbers."

        return try {
            val date = LocalDate.of(year, month, day)
            if (date.isAfter(LocalDate.now())) "Date cannot be in the future." else null
        } catch (e: Exception) {
            "Invalid date."
        }
    }

    // ---------------------- SAVE ANIMAL ---------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAnimal() {
        val form = _animalForm.value ?: AnimalForm()

        if (form.name.isBlank()) { _error.value = "Please fill in the name."; return }
        if (form.breed.isBlank()) { _error.value = "Select a breed."; return }
        if (form.size.isBlank()) { _error.value = "Select a size."; return }
        if (form.species.isBlank()) { _error.value = "Select a species."; return }

        val birthError = validateBirthDate(form.birthDate)
        if (birthError != null) { _error.value = birthError; return }

        val shelterFirebaseUid = _currentShelterFirebaseUid.value ?: run {
            _error.value = "Shelter Firebase UID not available"
            return
        }

        val images = selectedImages.value ?: emptyList()
        if (images.isEmpty()) {
            _error.value = "Add at least one image."
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

                // Firebase + Room
                val result = animalRepository.createAnimal(newAnimal)

                result.onSuccess {
                    _animalForm.value = AnimalForm()
                    clearImages()
                    _availableBreeds.value = emptyList()

                    _message.value = "Animal created successfully!"
                    _error.value = null
                }

                result.onFailure { e ->
                    _error.value = "Error saving animal: ${e.message}"
                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------- UTIL FUNCS ----------------------
    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}