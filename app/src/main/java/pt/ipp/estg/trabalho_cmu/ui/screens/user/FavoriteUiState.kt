package pt.ipp.estg.trabalho_cmu.ui.screens.user

import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite

/**
 * Represents all possible UI states for Favorite-related operations.
 *
 * Used by FavoriteViewModel to communicate loading, success,
 * and error states to the UI layer.
 *
 * States:
 * - Initial: No operation running
 * - Loading: Operation in progress
 * - Success: Generic success
 * - FavoriteAdded: Favorite successfully added
 * - FavoriteRemoved: Favorite successfully removed
 * - Error: An error occurred with a user-readable message
 */
sealed class FavoriteUiState {
    object Initial : FavoriteUiState()
    object Loading : FavoriteUiState()
    object Success : FavoriteUiState()
    data class FavoriteAdded(val favorite: Favorite) : FavoriteUiState()
    object FavoriteRemoved : FavoriteUiState()
    data class Error(val message: String) : FavoriteUiState()
}