package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import javax.inject.Inject

data class UserUiState(
    val animals: List<Animal> = emptyList(),
    val favorites: List<Animal> = emptyList(),
    val selectedAnimal: Animal? = null
)

@HiltViewModel
class UserViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState

    init {
        carregarAnimais()
    }

    private fun carregarAnimais() {
        // Aqui podes puxar de Room ou Firebase depois
        val lista = listOf(
            Animal("1", "Luna", 2, "https://placekitten.com/300/300", "Gata muito dócil"),
            Animal("2", "Bolinhas", 3, "https://placekitten.com/301/300", "Adora brincar"),
            Animal("3", "Rocky", 1, "https://placekitten.com/302/300", "Muito energético")
        )
        _uiState.value = _uiState.value.copy(animals = lista)
    }

    fun toggleFavorite(animal: Animal) {
        viewModelScope.launch {
            val favoritos = _uiState.value.favorites.toMutableList()
            if (favoritos.any { it.id == animal.id }) {
                favoritos.removeAll { it.id == animal.id }
            } else {
                favoritos.add(animal)
            }
            _uiState.value = _uiState.value.copy(favorites = favoritos)
        }
    }

    fun selecionarAnimal(id: String) {
        val animal = _uiState.value.animals.find { it.id == id }
        _uiState.value = _uiState.value.copy(selectedAnimal = animal)
    }
}
