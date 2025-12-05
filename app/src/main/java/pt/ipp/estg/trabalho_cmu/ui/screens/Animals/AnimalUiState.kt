package pt.ipp.estg.trabalho_cmu.ui.screens.Animals


import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Estados possíveis para operações relacionadas com Animais.
 * Ficheiro isolado.
 */
sealed class AnimalUiState {
    object Initial : AnimalUiState()
    object Loading : AnimalUiState()
    object Success : AnimalUiState()
    data class AnimalCreated(val animal: Animal) : AnimalUiState()
    data class Error(val message: String) : AnimalUiState()
}