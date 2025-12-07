package pt.ipp.estg.trabalho_cmu.ui.screens.Activity

import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity

sealed class ActivityUiState {
    object Initial : ActivityUiState()
    object Loading : ActivityUiState()
    object Success : ActivityUiState()
    data class ActivityScheduled(val activity: Activity) : ActivityUiState()
    object ActivityUpdated : ActivityUiState()
    object ActivityDeleted : ActivityUiState()
    data class Error(val message: String) : ActivityUiState()
}