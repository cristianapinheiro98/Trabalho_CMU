package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Represents the different UI states for actions performed by shelters:
 *
 * - Creating animals
 * - Approving/rejecting requests
 */
sealed class ShelterMngUiState {
    object Initial : ShelterMngUiState()
    object Loading : ShelterMngUiState()
    data class AnimalCreated(val animal: Animal) : ShelterMngUiState()
    object RequestApproved : ShelterMngUiState()
    object RequestRejected : ShelterMngUiState()
    data class Error(val message: String) : ShelterMngUiState()
}