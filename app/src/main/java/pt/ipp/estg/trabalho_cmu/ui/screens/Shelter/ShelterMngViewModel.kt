package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterOwnershipRequestRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.BreedRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel responsible for managing shelter-related operations:
 * - Managing adoption (ownership) requests
 * - Creating new animals
 * - Handling breeds and form validation
 */
class ShelterMngViewModel(application: Application) : AndroidViewModel(application) {

    private val ownershipRepository = ShelterOwnershipRequestRepository(
        AppDatabase.getDatabase(application).ownershipDao()
    )

    private val animalRepository = AnimalRepository(
        AppDatabase.getDatabase(application).animalDao()
    )

    private val breedRepository = BreedRepository()

    private val _requests = MutableLiveData<List<AdoptionRequest>>(emptyList())
    val requests: LiveData<List<AdoptionRequest>> = _requests

    private val _animalForm = MutableLiveData(AnimalForm())
    val animalForm: LiveData<AnimalForm> = _animalForm

    private val _availableBreeds = MutableLiveData<List<Breed>>(emptyList())
    val availableBreeds: LiveData<List<Breed>> = _availableBreeds

    private val _isLoadingBreeds = MutableLiveData(false)
    val isLoadingBreeds: LiveData<Boolean> = _isLoadingBreeds

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        loadOwnershipRequests()
    }

    /**
     * Loads all ownership/adoption requests pending review.
     */
    fun loadOwnershipRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Replace this with your actual DAO logic returning LiveData<List<Ownership>>
                // For now, simulating requests
                _requests.value = listOf(
                    AdoptionRequest("1", "José Lemos", "joselemos@example.com", "Bolinhas"),
                    AdoptionRequest("2", "Maria Silva", "maria@example.com", "Luna")
                )
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading requests: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Approves an ownership/adoption request.
     */
    fun approveRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                ownershipRepository.approveOwnershipRequest(request.id.toInt())
                _requests.value = _requests.value?.filterNot { it.id == request.id }
                _message.value = "Request approved successfully!"
            } catch (e: Exception) {
                _error.value = "Error approving request: ${e.message}"
            }
        }
    }

    /**
     * Rejects an ownership/adoption request.
     */
    fun rejectRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                ownershipRepository.rejectOwnershipRequest(request.id.toInt())
                _requests.value = _requests.value?.filterNot { it.id == request.id }
                _message.value = "Request rejected."
            } catch (e: Exception) {
                _error.value = "Error rejecting request: ${e.message}"
            }
        }
    }

    /**
     * Loads breeds for a given species (dog/cat).
     */
    private fun loadBreedsBySpecies(species: String) {
        if (species.isBlank()) {
            _availableBreeds.value = emptyList()
            return
        }

        _isLoadingBreeds.value = true
        breedRepository.getBreedsBySpecies(
            species = species,
            onSuccess = { breeds ->
                _availableBreeds.value = breeds
                _isLoadingBreeds.value = false
            },
            onError = { errorMsg ->
                _error.value = errorMsg
                _availableBreeds.value = emptyList()
                _isLoadingBreeds.value = false
            }
        )
    }

    fun loadDogBreeds() {
        _isLoadingBreeds.value = true
        breedRepository.getDogBreeds(
            onSuccess = { breeds ->
                _availableBreeds.value = breeds
                _isLoadingBreeds.value = false
            },
            onError = { errorMsg ->
                _error.value = errorMsg
                _isLoadingBreeds.value = false
            }
        )
    }

    fun loadCatBreeds() {
        _isLoadingBreeds.value = true
        breedRepository.getCatBreeds(
            onSuccess = { breeds ->
                _availableBreeds.value = breeds
                _isLoadingBreeds.value = false
            },
            onError = { errorMsg ->
                _error.value = errorMsg
                _isLoadingBreeds.value = false
            }
        )
    }

    fun onNameChange(value: String) = updateForm { copy(name = value) }
    fun onBreedChange(value: String) = updateForm { copy(breed = value) }
    fun onSpeciesChange(value: String) {
        updateForm { copy(species = value) }
        loadBreedsBySpecies(value)
    }
    fun onSizeChange(value: String) = updateForm { copy(size = value) }
    fun onBirthDateChange(value: String) = updateForm { copy(birthDate = value) }
    fun onDescriptionChange(value: String) = updateForm { copy(description = value) }
    fun onImageUrlChange(value: String) {
        val parsed = value.toIntOrNull() ?: 0
        updateForm { copy(imageUrl = parsed) }
    }

    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validateBirthDate(birthDate: String): String? {

        if (birthDate.isBlank()) {
            return "A data de nascimento é obrigatória."
        }

        // Formato esperado: DD/MM/YYYY
        val parts = birthDate.split("/")

        if (parts.size != 3) {
            return "A data deve estar no formato DD/MM/AAAA."
        }

        val (dayStr, monthStr, yearStr) = parts

        val day = dayStr.toIntOrNull()
        val month = monthStr.toIntOrNull()
        val year = yearStr.toIntOrNull()

        // Validar se são números
        if (day == null || month == null || year == null) {
            return "Dia, mês e ano devem ser números."
        }

        // Validar ranges
        if (day !in 1..31) return "Dia inválido."
        if (month !in 1..12) return "Mês inválido."
        if (year < 1900) return "Ano inválido."

        // Validar se a data existe
        return try {
            val date = LocalDate.of(year, month, day)

            // Data do futuro não é permitida
            if (date.isAfter(LocalDate.now())) {
                "A data de nascimento não pode ser no futuro."
            } else {
                null // Está válida!
            }

        } catch (e: Exception) {
            "Data inválida."
        }
    }



    /**
     * Saves a new animal record to the local database.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAnimal() {
        val form = _animalForm.value ?: AnimalForm()

        if (form.name.isBlank() || form.breed.isBlank()) {
            _error.value = "Please fill in at least Name and Breed."
            return
        }

        if (form.species.isBlank()) {
            _error.value = "Select a species."
            return
        }
        val dataError=validateBirthDate(form.birthDate)
        if(dataError!=null){
            _error.value=dataError
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val newAnimal = Animal(
                    name = form.name.trim(),
                    breed = form.breed.trim(),
                    species = form.species.trim(),
                    size = form.size.ifBlank { "Medium" }.trim(),
                    birthDate = form.birthDate.trim(),
                    imageUrl = listOf(form.imageUrl),
                    description = form.description.trim(),
                    shelterId = 1
                )

                animalRepository.insertAnimal(newAnimal)

                _animalForm.value = AnimalForm()
                _availableBreeds.value = emptyList()

                _message.value = "Animal saved successfully!"
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error saving animal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}
