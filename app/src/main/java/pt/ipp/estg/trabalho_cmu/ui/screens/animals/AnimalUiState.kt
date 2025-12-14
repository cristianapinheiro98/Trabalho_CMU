package pt.ipp.estg.trabalho_cmu.ui.screens.animals

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Represents the UI state for animal-related operations such as:
 * - Loading states
 * - Creation success
 * - Errors
 */
sealed class AnimalUiState {
    object Initial : AnimalUiState()
    object Loading : AnimalUiState()
    data class AnimalCreated(val animal: Animal) : AnimalUiState()
    data class Error(val message: String) : AnimalUiState()
    object Success : AnimalUiState()
}
