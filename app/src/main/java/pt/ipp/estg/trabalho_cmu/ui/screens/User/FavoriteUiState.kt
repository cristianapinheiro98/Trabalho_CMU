package pt.ipp.estg.trabalho_cmu.ui.screens.User

import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite

/**
 * Estados possíveis para operações relacionadas com Favoritos.
 * Ficheiro isolado.
 */
sealed class FavoriteUiState {
    object Initial : FavoriteUiState()
    object Loading : FavoriteUiState()
    object Success : FavoriteUiState()
    data class FavoriteAdded(val favorite: Favorite) : FavoriteUiState()
    object FavoriteRemoved : FavoriteUiState()
    data class Error(val message: String) : FavoriteUiState()
}