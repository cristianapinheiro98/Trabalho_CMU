package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.repository.*

class ShelterMngViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val shelterOwnershipRequestRepository =
        ShelterOwnershipRequestRepository(db.ownershipDao())

    private val ownershipRepository = OwnershipRepository(db.ownershipDao())
    private val animalRepository = AnimalRepository(db.animalDao())
    private val shelterRepository = ShelterRepository(db.shelterDao())
    private val userRepository = UserRepository(db.userDao())
    private val breedRepository = BreedRepository()

    // ---------------------- FORM ----------------------
    private val _animalForm = MutableLiveData(AnimalForm())
    val animalForm: LiveData<AnimalForm> = _animalForm

    // ---------------------- BREEDS ----------------------
    private val _availableBreeds = MutableLiveData<List<Breed>>(emptyList())
    val availableBreeds: LiveData<List<Breed>> = _availableBreeds

    private val _isLoadingBreeds = MutableLiveData(false)
    val isLoadingBreeds: LiveData<Boolean> = _isLoadingBreeds

    // ---------------------- UI STATES ----------------------
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // ---------------------- SHELTER ----------------------
    private val _currentShelterId = MutableLiveData<Int?>(null)

    // ---------------------- IMAGENS ----------------------
    private val _selectedImages = MutableLiveData<List<String>>(emptyList())
    val selectedImages: LiveData<List<String>> = _selectedImages

    fun addImageUrl(url: String) {
        val current = _selectedImages.value ?: emptyList()
        _selectedImages.value = current + url
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
    }

    // ---------------------- LOAD SHELTER ID ----------------------
    fun getShelterIdByUserId(userId: Int) {
        viewModelScope.launch {
            val shelterId = userRepository.getShelterIdByUserId(userId) ?: userId
            _currentShelterId.value = shelterId

            try {
                shelterRepository.getShelterById(shelterId)
            } catch (e: Exception) {
                println("Erro ao verificar shelter: ${e.message}")
            }
        }
    }

    // ---------------------- LISTAR PEDIDOS ----------------------
    val requests: LiveData<List<AdoptionRequest>> =
        _currentShelterId.switchMap { shelterId ->
            if (shelterId != null) {
                MediatorLiveData<List<AdoptionRequest>>().apply {
                    addSource(
                        shelterOwnershipRequestRepository.getAllOwnershipRequestsByShelter(
                            shelterId
                        )
                    ) { ownerships ->
                        viewModelScope.launch {
                            value = ownerships.mapNotNull {
                                try {
                                    convertToAdoptionRequest(it)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                    }
                }
            } else MutableLiveData(emptyList())
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

    // ---------------------- APROVAR PEDIDO ----------------------
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

    // ---------------------- BREEDS ----------------------
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

    // ---------------------- FORM HANDLERS ----------------------
    fun onNameChange(value: String) = updateForm { copy(name = value) }
    fun onBreedChange(value: String) = updateForm { copy(breed = value) }
    fun onSpeciesChange(value: String) {
        updateForm { copy(species = value) }
        loadBreedsBySpecies(value)
    }
    fun onSizeChange(value: String) = updateForm { copy(size = value) }
    fun onBirthDateChange(value: String) = updateForm { copy(birthDate = value) }

    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }

    // ---------------------- CREATE ANIMAL ----------------------
    fun saveAnimal() {
        val form = _animalForm.value ?: AnimalForm()

        if (form.name.isBlank()) { _error.value = "Por favor preenche o nome."; return }
        if (form.breed.isBlank()) { _error.value = "Seleciona uma raça."; return }
        if (form.size.isBlank()) { _error.value = "Seleciona um tamanho."; return }
        if (form.species.isBlank()) { _error.value = "Seleciona uma espécie."; return }

        val shelterId = _currentShelterId.value ?: run {
            _error.value = "Shelter ID não disponível"
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

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}
