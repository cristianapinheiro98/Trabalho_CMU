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
import pt.ipp.estg.trabalho_cmu.data.models.PedidoAdocao
import pt.ipp.estg.trabalho_cmu.data.repository.AdminRepository

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdminRepository(AppDatabase.getDatabase(application))

    private val _pedidos = MutableLiveData<List<PedidoAdocao>>(emptyList())
    val pedidos: LiveData<List<PedidoAdocao>> = _pedidos

    private val _animalForm = MutableLiveData(AnimalForm())
    val animalForm: LiveData<AnimalForm> = _animalForm

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init { carregarPedidos() }

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

    // -------- Form (nomes iguais à entidade) --------
    fun onNameChange(value: String)     = updateForm { copy(name = value) }
    fun onBreedChange(value: String)    = updateForm { copy(breed = value) }
    fun onSpeciesChange(value: String)  = updateForm { copy(species = value) }
    fun onSizeChange(value: String)     = updateForm { copy(size = value) }
    fun onBirthDateChange(value: String)= updateForm { copy(birthDate = value) }
    fun onImageUrlChange(value: String) {
        val parsed = value.toIntOrNull() ?: 0
        updateForm { copy(imageUrl = parsed) }
    }


    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _animalForm.value = (_animalForm.value ?: AnimalForm()).block()
    }

    fun guardarAnimal() {
        val form = _animalForm.value ?: AnimalForm()

        if (form.name.isBlank() || form.breed.isBlank()) {
            _message.value = "Preenche pelo menos o Nome e a Raça."
            _error.value = null
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val novoAnimal = Animal(
                    name = form.name.trim(),
                    breed = form.breed.trim(),
                    species = form.species.ifBlank { "Desconhecida" }.trim(),
                    size = form.size.ifBlank { "Médio" }.trim(),
                    birthDate = form.birthDate.trim(),
                    imageUrl = form.imageUrl,
                    shelterId = 1
                )
                repository.addAnimal(novoAnimal)

                _animalForm.value = AnimalForm()
                _message.value = "Animal guardado com sucesso!"
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao guardar animal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError()   { _error.value = null }
}
