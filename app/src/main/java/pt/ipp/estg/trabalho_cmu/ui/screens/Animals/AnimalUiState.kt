package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

sealed class AnimalUiState {
    object Initial : AnimalUiState()
    object Loading : AnimalUiState()
    data class AnimalCreated(val animal: Animal) : AnimalUiState()
    data class Error(val message: String) : AnimalUiState()
    object Success : AnimalUiState()  // Útil para operações futuras
}
