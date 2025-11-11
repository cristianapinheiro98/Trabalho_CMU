package pt.ipp.estg.trabalho_cmu.ui.screens.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.models.PedidoAdocao
import pt.ipp.estg.trabalho_cmu.data.repository.AdminRepository
import pt.ipp.estg.trabalho_cmu.data.repository.BreedRepository

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdminRepository(AppDatabase.getDatabase(application))
    private val breedRepository = BreedRepository() // 🆕 Repository para raças

    private val _pedidos = MutableLiveData<List<PedidoAdocao>>(emptyList())
    val pedidos: LiveData<List<PedidoAdocao>> = _pedidos

    private val _animalForm = MutableLiveData(AnimalForm())
    val animalForm: LiveData<AnimalForm> = _animalForm

    // 🆕 Estado das raças
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
        carregarPedidos()
    }

    fun carregarPedidos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _pedidos.value = repository.getAllPedidos()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar pedidos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun aprovarPedido(pedido: PedidoAdocao) = atualizarPedido(pedido, "Pedido aprovado com sucesso!")
    fun rejeitarPedido(pedido: PedidoAdocao) = atualizarPedido(pedido, "Pedido rejeitado.")

    private fun atualizarPedido(pedido: PedidoAdocao, msg: String) {
        viewModelScope.launch {
            try {
                repository.deletePedidoById(pedido.id) // placeholder
                _pedidos.value = _pedidos.value?.filterNot { it.id == pedido.id }
                _message.value = msg
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar pedido: ${e.message}"
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

    /**
     * Carregar raças de cães manualmente
     */
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

    /**
     * Carregar raças de gatos manualmente
     */
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

    fun guardarAnimal() {
        val form = _animalForm.value ?: AnimalForm()

        // Validação
        if (form.name.isBlank() || form.breed.isBlank()) {
            _error.value = "Preenche pelo menos o Nome e a Raça."
            return
        }

        if (form.species.isBlank()) {
            _error.value = "Seleciona a Espécie."
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val novoAnimal = Animal(
                    name = form.name.trim(),
                    breed = form.breed.trim(),
                    species = form.species.trim(),
                    size = form.size.ifBlank { "Médio" }.trim(),
                    birthDate = form.birthDate.trim(),
                    imageUrl = listOf(form.imageUrl),
                    shelterId = 1
                )
                repository.addAnimal(novoAnimal)

                // Limpar form após sucesso
                _animalForm.value = AnimalForm()
                _availableBreeds.value = emptyList()

                _message.value = "Animal guardado com sucesso!"
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao guardar animal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun clearError() {
        _error.value = null
    }
}