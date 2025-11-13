package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.repository.*
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterOwnershipRequestRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.BreedRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import pt.ipp.estg.trabalho_cmu.data.repository.UserRepository
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository

class ShelterMngViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val shelterOwnershipRequestRepository =
        ShelterOwnershipRequestRepository(db.ownershipDao())

    private val ownershipRepository = OwnershipRepository(db.ownershipDao())
    private val animalRepository = AnimalRepository(db.animalDao())

    private val shelterRepository = ShelterRepository(db.shelterDao())

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

    fun addImageUrl(url: String) {
        _selectedImages.value = _selectedImages.value!! + url
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
    }

    // ---------------------- SHELTER ID ---------------------
    private val _currentShelterId = MutableLiveData<Int?>(null)
    val currentShelterId: LiveData<Int?> = _currentShelterId

    /**
     * Define o shelterId vindo do AuthViewModel.getCurrentUserId()
     */
    fun setShelterId(id: Int) {
        _currentShelterId.value = id
    }

    // ---------------------- PEDIDOS DE ADOÇÃO ---------------
    val requests: LiveData<List<AdoptionRequest>> =
        _currentShelterId.switchMap { shelterId ->
            if (shelterId != null) {
                MediatorLiveData<List<AdoptionRequest>>().apply {
                    addSource(
                        shelterOwnershipRequestRepository
                            .getAllOwnershipRequestsByShelter(shelterId)
                    ) { ownerships ->
                        viewModelScope.launch {
                            value = ownerships.mapNotNull {
                                convertToAdoptionRequest(it)
                            } catch (e: Exception) {
                                println("❌ Erro ao converter ownership: ${e.message}")
                                null
                            }
                        }
                    }
                }
            } else {
                MutableLiveData(emptyList())
            }
        }

    private suspend fun convertToAdoptionRequest(ownership: Ownership): AdoptionRequest {
        val user = userRepository.getUserById(ownership.userId)
        val animal = animalRepository.getAnimalById(ownership.animalId)

        return AdoptionRequest(
            id = ownership.id.toString(),
            nome = user?.name ?: "Desconhecido",
            email = user?.email ?: "N/A",
            animal = animal?.name ?: "Animal #${ownership.animalId}"
        )
    }

    // ---------------------- APROVAR / REJEITAR --------------

    fun approveRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val ownershipId = request.id.toIntOrNull() ?: return@launch
                shelterOwnershipRequestRepository.approveOwnershipRequest(ownershipId)

                val ownership = ownershipRepository.getOwnershipById(ownershipId)
                val animalId = ownership?.animalId ?: return@launch

                animalRepository.changeAnimalStatusToOwned(animalId)

                _message.value = "Pedido aprovado com sucesso!"
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Erro ao aprovar pedido: ${e.message}"
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
                shelterOwnershipRequestRepository.rejectOwnershipRequest(ownershipId)

                _message.value = "Pedido rejeitado"
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Erro ao rejeitar pedido: ${e.message}"
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
    fun onImageUrlChange(value: String) {
        val parsed = value.toIntOrNull() ?: 0
        updateForm { copy(imageUrl = parsed) }
    }

    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }

    // ---------------------- SAVE ANIMAL ---------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun validateBirthDate(birthDate: String): String? {

        if (birthDate.isBlank()) {
            return "A data de nascimento é obrigatória."
        }

        val parts = birthDate.split("/")

        if (parts.size != 3) {
            return "A data deve estar no formato DD/MM/AAAA."
        }

        val (dayStr, monthStr, yearStr) = parts

        val day = dayStr.toIntOrNull()
        val month = monthStr.toIntOrNull()
        val year = yearStr.toIntOrNull()

        if (day == null || month == null || year == null) {
            return "Dia, mês e ano devem ser números."
        }


        if (day !in 1..31) return "Dia inválido."
        if (month !in 1..12) return "Mês inválido."

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

        if (form.name.isBlank()) {
            _error.value = "Por favor preenche o nome."; return
        }
        if (form.breed.isBlank()) {
            _error.value = "Seleciona uma raça."; return
        }
        if (form.size.isBlank()) {
            _error.value = "Seleciona um tamanho."; return
        }
        if (form.species.isBlank()) {
            _error.value = "Seleciona uma espécie."; return
        }

        val shelterId = _currentShelterId.value ?: run {
            _error.value = "Shelter ID não disponível"
            return
        }
        val dataError=validateBirthDate(form.birthDate)
        if(dataError!=null){
            _error.value=dataError
            return
        }

        val images = selectedImages.value ?: emptyList()
        if (images.isEmpty()) {
            _error.value = "Adiciona pelo menos uma imagem."
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val newAnimal = Animal(
                    name = form.name.trim(),
                    breed = form.breed.trim(),
                    species = form.species.trim(),
                    size = form.size.trim(),
                    birthDate = form.birthDate.trim(),
                    imageUrls = images,
                    imageUrl = listOf(form.imageUrl),
                    description = form.description.trim(),

                    shelterId = shelterId
                )

                animalRepository.insertAnimal(newAnimal)

                _animalForm.value = AnimalForm()
                clearImages()
                _availableBreeds.value = emptyList()

                _message.value = "Animal criado com sucesso!"
                _error.value = null

            } catch (e: Exception) {
                _error.value = "Erro ao salvar o animal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------- UTIL FUNCS ----------------------
    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}
