package pt.ipp.estg.trabalho_cmu.ui.screens.ownership

import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership

/**
 * Represents the different UI states of the ownership (adoption) creation flow.
 *
 * This sealed class is used by the UI layer to reactively update the screen
 * based on the current operation state. It enables:
 * - Displaying loading indicators
 * - Showing success or error messages
 * - Resetting or initializing the flow
 *
 * Variants:
 *  - Initial: Default state before any action is taken
 *  - Loading: An ownership request is being processed
 *  - Success: General success state (not used directly in this flow)
 *  - OwnershipCreated: A new ownership was successfully created
 *  - Error: The process failed with a specific message
 */
sealed class OwnershipUiState {
    object Initial : OwnershipUiState()
    object Loading : OwnershipUiState()
    object Success : OwnershipUiState()
    data class OwnershipCreated(val ownership: Ownership) : OwnershipUiState()
    data class Error(val message: String) : OwnershipUiState()
}