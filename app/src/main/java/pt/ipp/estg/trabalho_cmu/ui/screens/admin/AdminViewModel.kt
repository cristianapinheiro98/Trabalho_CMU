package pt.ipp.estg.trabalho_cmu.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.PedidoAdocao
import javax.inject.Inject

data class AdminUiState(
    val pedidos: List<PedidoAdocao> = emptyList(),
    val dialogMessage: String? = null,
    val isSuccessDialog: Boolean = false,
    val animalForm: AnimalForm = AnimalForm(),
)

@HiltViewModel
class AdminViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    init {
        carregarPedidos()
    }

    // 🔹 Pedidos de adoção
    private fun carregarPedidos() {
        _uiState.value = _uiState.value.copy(
            pedidos = listOf(
                PedidoAdocao("1", "José Lemos", "joselemos@example.com", "Bolinhas"),
                PedidoAdocao("2", "Maria Silva", "maria@example.com", "Luna")
            )
        )
    }

    fun aprovarPedido(pedido: PedidoAdocao) = atualizarPedido(pedido, "Pedido aceite com sucesso!", true)
    fun rejeitarPedido(pedido: PedidoAdocao) = atualizarPedido(pedido, "Pedido rejeitado!", false)

    private fun atualizarPedido(p: PedidoAdocao, msg: String, sucesso: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pedidos = _uiState.value.pedidos.filterNot { it.id == p.id },
                dialogMessage = msg,
                isSuccessDialog = sucesso
            )
        }
    }

    fun fecharDialogo() {
        _uiState.value = _uiState.value.copy(dialogMessage = null)
    }

    // 🔹 Formulário de registo de animal
    fun onNomeChange(value: String) = updateForm { copy(nome = value) }
    fun onRacaChange(value: String) = updateForm { copy(raca = value) }
    fun onCorChange(value: String) = updateForm { copy(cor = value) }
    fun onDataNascimentoChange(value: String) = updateForm { copy(dataNascimento = value) }

    private inline fun updateForm(block: AnimalForm.() -> AnimalForm) {
        _uiState.value = _uiState.value.copy(animalForm = _uiState.value.animalForm.block())
    }

    fun guardarAnimal() {
        val form = _uiState.value.animalForm
        if (form.nome.isBlank() || form.raca.isBlank()) {
            _uiState.value = _uiState.value.copy(
                dialogMessage = "Preenche todos os campos obrigatórios.",
                isSuccessDialog = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                dialogMessage = "Animal guardado com sucesso!",
                isSuccessDialog = true,
                animalForm = AnimalForm() // limpa o formulário
            )
        }
    }
}
