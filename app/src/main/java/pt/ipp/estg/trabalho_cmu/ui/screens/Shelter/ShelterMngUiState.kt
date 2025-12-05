package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Estados para gestão de abrigos (admin)
 */
sealed class ShelterMngUiState {
    object Initial : ShelterMngUiState()
    object Loading : ShelterMngUiState()
    data class AnimalCreated(val animal: Animal) : ShelterMngUiState()
    object RequestApproved : ShelterMngUiState()
    object RequestRejected : ShelterMngUiState()
    data class Error(val message: String) : ShelterMngUiState()
}