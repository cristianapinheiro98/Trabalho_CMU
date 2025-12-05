package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import android.os.Build
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
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong
import java.time.LocalDate

class ShelterMngViewModel(application: Application) : AndroidViewModel(application) {

    private val ownershipRepository = DatabaseModule.provideOwnershipRepository(application)
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)
    private val userRepository = DatabaseModule.provideUserRepository(application)
    private val breedRepository = BreedRepository()

    // --- UI STATE ---
    private val _uiState = MutableLiveData<ShelterMngUiState>(ShelterMngUiState.Initial)
    val uiState: LiveData<ShelterMngUiState> = _uiState

    // Helpers individuais
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // --- FORM DATA ---
    private val _animalForm = MutableLiveData(AnimalForm())
    val animalForm: LiveData<AnimalForm> = _animalForm

    // --- BREEDS ---
    private val _availableBreeds = MutableLiveData<List<Breed>>(emptyList())
    val availableBreeds: LiveData<List<Breed>> = _availableBreeds
    private val _isLoadingBreeds = MutableLiveData(false)
    val isLoadingBreeds: LiveData<Boolean> = _isLoadingBreeds

    // --- IMAGES ---
    private val _selectedImages = MutableLiveData<List<String>>(emptyList())
    val selectedImages: LiveData<List<String>> = _selectedImages
    private val _isUploadingImages = MutableLiveData(false)
    val isUploadingImages: LiveData<Boolean> = _isUploadingImages

    fun setUploadingImages(value: Boolean) { _isUploadingImages.value = value }
    fun addImageUrl(url: String) {
        _selectedImages.value = (_selectedImages.value ?: emptyList()) + url
        _isUploadingImages.value = false
    }
    fun clearImages() { _selectedImages.value = emptyList() }

    // --- SHELTER ID ---
    private val _currentShelterId = MutableLiveData<String?>()

    fun setShelterFirebaseUid(shelterId: String) {
        _currentShelterId.value = shelterId
        viewModelScope.launch {
            try {
                ownershipRepository.syncPendingOwnerships(shelterId)
            } catch (e: Exception) {
                _error.value = "Erro sync: ${e.message}"
            }
        }
    }

    // --- REQUESTS LIST ---
    val requests = MediatorLiveData<List<AdoptionRequest>>()

    init {
        requests.addSource(_currentShelterId) { id ->
            if (id != null) {
                val source = ownershipRepository.getPendingOwnershipsByShelter(id)
                requests.addSource(source) { ownershipList ->
                    viewModelScope.launch {
                        val converted = ownershipList.mapNotNull { convertToAdoptionRequest(it) }
                        requests.value = converted
                    }
                }
            } else {
                requests.value = emptyList()
            }
        }
    }

    private suspend fun convertToAdoptionRequest(ownership: Ownership): AdoptionRequest? {
        return try {
            val user = userRepository.getUserById(ownership.userId)
            val animal = animalRepository.getAnimalById(ownership.animalId)
            if (user != null && animal != null) {
                AdoptionRequest(ownership.id, user.name, user.email, animal.name)
            } else null
        } catch (e: Exception) { null }
    }

    // --- APPROVE / REJECT ---
    fun approveRequest(request: AdoptionRequest) = viewModelScope.launch {
        _isLoading.value = true
        _uiState.value = ShelterMngUiState.Loading
        try {
            val ownership = ownershipRepository.getOwnershipById(request.id)
            if (ownership != null) {
                ownershipRepository.approveOwnershipRequest(request.id, ownership.animalId)
                    .onSuccess {
                        _uiState.value = ShelterMngUiState.RequestApproved
                        _message.value = "Pedido aprovado!"
                    }
                    .onFailure {
                        _uiState.value = ShelterMngUiState.Error(it.message ?: "Erro")
                        _error.value = it.message
                    }
            } else {
                _error.value = "Pedido não encontrado"
            }
        } catch (e: Exception) {
            _error.value = "Erro: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun rejectRequest(request: AdoptionRequest) = viewModelScope.launch {
        _isLoading.value = true
        _uiState.value = ShelterMngUiState.Loading

        ownershipRepository.rejectOwnershipRequest(request.id)
            .onSuccess {
                _uiState.value = ShelterMngUiState.RequestRejected
                _message.value = "Pedido rejeitado."
            }
            .onFailure {
                _uiState.value = ShelterMngUiState.Error(it.message ?: "Erro")
                _error.value = it.message
            }
        _isLoading.value = false
    }

    // --- BREEDS & FORM ---
    private fun loadBreedsBySpecies(species: String) {
        if (species.isBlank()) { _availableBreeds.value = emptyList(); return }
        _isLoadingBreeds.value = true
        breedRepository.getBreedsBySpecies(species,
            onSuccess = { _availableBreeds.postValue(it); _isLoadingBreeds.postValue(false) },
            onError = { _error.postValue(it); _availableBreeds.postValue(emptyList()); _isLoadingBreeds.postValue(false) }
        )
    }

    fun onNameChange(v: String) = updateForm { copy(name = v) }
    fun onBreedChange(v: String) = updateForm { copy(breed = v) }
    fun onSpeciesChange(v: String) { updateForm { copy(species = v) }; loadBreedsBySpecies(v) }
    fun onSizeChange(v: String) = updateForm { copy(size = v) }
    fun onBirthDateChange(v: String) = updateForm { copy(birthDate = v) }
    fun onDescriptionChange(v: String) = updateForm { copy(description = v) }
    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) { _animalForm.value = (_animalForm.value ?: AnimalForm()).block() }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validateBirthDate(birthDate: String): String? {
        val ctx = getApplication<Application>() // Contexto para strings
        if (birthDate.isBlank()) return ctx.getString(R.string.error_birthdate_required)
        val parts = birthDate.split("/")
        if (parts.size != 3) return ctx.getString(R.string.error_birthdate_format)

        val day = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val year = parts[2].toIntOrNull()

        if (day !in 1..31) return ctx.getString(R.string.error_invalid_day)
        if (month !in 1..12) return ctx.getString(R.string.error_invalid_month)
        if (day == null || month == null || year == null) return ctx.getString(R.string.error_invalid_date_numbers)

        return try {
            val date = LocalDate.of(year, month, day)
            if (date.isAfter(LocalDate.now())) ctx.getString(R.string.error_future_date) else null
        } catch (e: Exception) { "Data inválida." }
    }

    // --- SAVE ANIMAL  ---
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAnimal() {
        val form = _animalForm.value ?: return
        val shelterId = _currentShelterId.value ?: run { _error.value = "Erro: Sem abrigo logado"; return }

        val ctx = getApplication<Application>()

        if (form.name.isBlank()) { _error.value = ctx.getString(R.string.error_name_required); return }
        if (form.breed.isBlank()) { _error.value = ctx.getString(R.string.error_breed_required); return }
        if (form.size.isBlank()) { _error.value = ctx.getString(R.string.error_size_required); return }
        if (form.species.isBlank()) { _error.value = ctx.getString(R.string.error_species_required); return }

        val birthError = validateBirthDate(form.birthDate)
        if (birthError != null) { _error.value = birthError; return }

        val images = _selectedImages.value ?: emptyList()
        if (images.isEmpty()) {
            _error.value = ctx.getString(R.string.error_add_image)
            return
        }

        // Converter a String do form para Long
        val birthDateLong = dateStringToLong(form.birthDate.trim())


        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ShelterMngUiState.Loading
            try {
                val animal = Animal(
                    id = "", name = form.name, breed = form.breed, species = form.species,
                    size = form.size, birthDate = birthDateLong, description = form.description,
                    imageUrls = images, shelterId = shelterId
                )

                animalRepository.createAnimal(animal)
                    .onSuccess {
                        animalRepository.syncAnimals()
                        _uiState.value = ShelterMngUiState.AnimalCreated(it)
                        _animalForm.value = AnimalForm()
                        clearImages()
                        _message.value = ctx.getString(R.string.success_animal_created)
                    }
                    .onFailure { e ->
                        _uiState.value = ShelterMngUiState.Error(e.message ?: "Erro")
                        _error.value = ctx.getString(R.string.error_save_animal) + " ${e.message}"
                    }
            } catch (e: Exception) {
                _error.value = ctx.getString(R.string.error_save_animal) + " ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
    fun resetState() { _uiState.value = ShelterMngUiState.Initial }
}