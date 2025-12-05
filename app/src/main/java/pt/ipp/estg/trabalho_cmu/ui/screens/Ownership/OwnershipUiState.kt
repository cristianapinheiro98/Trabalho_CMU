package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership

sealed class OwnershipUiState {
    object Initial : OwnershipUiState()
    object Loading : OwnershipUiState()
    object Success : OwnershipUiState() // Usado para sync ou operações sem retorno de dados
    data class OwnershipCreated(val ownership: Ownership) : OwnershipUiState()
    data class Error(val message: String) : OwnershipUiState()
}