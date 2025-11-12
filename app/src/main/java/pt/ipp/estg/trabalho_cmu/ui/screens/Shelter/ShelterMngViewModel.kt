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
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterOwnershipRequestRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.BreedRepository
import pt.ipp.estg.trabalho_cmu.data.repository.UserRepository
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository

class ShelterMngViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val ownershipRepository = ShelterOwnershipRequestRepository(db.ownershipDao())
    private val animalRepository = AnimalRepository(db.animalDao())

    private val shelterRepository = ShelterRepository(db.shelterDao())

    private val userRepository = UserRepository(db.userDao())
    private val breedRepository = BreedRepository()

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

    // ✅ Expõe uma função para definir o shelterId quando necessário
    private val _currentShelterId = MutableLiveData<Int?>(null)


    fun getShelterIdByUserId(userId: Int) {
        viewModelScope.launch {
            println("🔍 VIEWMODEL - userId recebido: $userId")
            val shelterId = userRepository.getShelterIdByUserId(userId) ?: userId
            _currentShelterId.value = shelterId
            println("🔍 VIEWMODEL - shelterId definido: $shelterId")

            // ✅ ADICIONE ESTA VERIFICAÇÃO
            // Verifica se o shelter realmente existe
            try {
                val shelter = shelterRepository.getShelterById(shelterId)
                if (shelter != null) {
                    println("✅ Shelter existe: ${shelter.name}")
                } else {
                    println("❌ ERRO: Shelter com ID $shelterId NÃO EXISTE!")
                    _error.value = "Shelter não encontrado. Por favor, faça logout e login novamente."
                }
            } catch (e: Exception) {
                println("❌ Erro ao verificar shelter: ${e.message}")
            }
        }
    }

    val requests: LiveData<List<AdoptionRequest>> = _currentShelterId.switchMap { shelterId ->
        if (shelterId != null) {
            MediatorLiveData<List<AdoptionRequest>>().apply {
                addSource(ownershipRepository.getAllOwnershipRequests(shelterId)) { ownerships ->
                    viewModelScope.launch {
                        value = ownerships.mapNotNull {
                            try {
                                convertToAdoptionRequest(it)
                            } catch (e: Exception) {
                                println("❌ Erro ao converter ownership: ${e.message}")
                                null
                            }
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

    fun approveRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val ownershipId = request.id.toIntOrNull() ?: run {
                    _error.value = "ID inválido"
                    return@launch
                }

                ownershipRepository.approveOwnershipRequest(ownershipId)
                _message.value = "Pedido aprovado com sucesso!"
                _error.value = null
                println("✅ Pedido $ownershipId aprovado")
            } catch (e: Exception) {
                _error.value = "Erro ao aprovar pedido: ${e.message}"
                println("❌ Erro ao aprovar: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectRequest(request: AdoptionRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val ownershipId = request.id.toIntOrNull() ?: run {
                    _error.value = "ID inválido"
                    return@launch
                }

                ownershipRepository.rejectOwnershipRequest(ownershipId)
                _message.value = "Pedido rejeitado"
                _error.value = null
                println("✅ Pedido $ownershipId rejeitado")
            } catch (e: Exception) {
                _error.value = "Erro ao rejeitar pedido: ${e.message}"
                println("❌ Erro ao rejeitar: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

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
    fun onImageUrlChange(value: String) {
        val parsed = value.toIntOrNull() ?: 0
        updateForm { copy(imageUrl = parsed) }
    }

    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }

    fun saveAnimal() {
        val form = _animalForm.value ?: AnimalForm()

        if (form.name.isBlank()) {
            _error.value = "Por favor preenche o nome."
            return
        }

        if (form.breed.isBlank()) {
            _error.value = "Seleciona uma raça."
            return
        }

        if (form.size.isBlank()) {
            _error.value = "Seleciona um tamanho."
            return
        }

        if (form.species.isBlank()) {
            _error.value = "Seleciona uma espécie."
            return
        }

        val shelterId = _currentShelterId.value ?: run {
            _error.value = "Shelter ID não disponível"
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
                    shelterId = shelterId
                )

                animalRepository.insertAnimal(newAnimal)

                _animalForm.value = AnimalForm()
                _availableBreeds.value = emptyList()

                _message.value = "Animal criado com sucesso!"
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao salvar o animal: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}