package pt.ipp.estg.trabalho_cmu.ui.screens.veterinarians

import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian

/**
 * UI State for the Veterinarians Screen.
 *
 * Represents the different states the screen can be in while displaying nearby veterinarians.
 */
sealed class VeterinariansUiState {

    /**
     * Initial loading state while obtaining user location.
     */
    object LoadingLocation : VeterinariansUiState()

    /**
     * State when location permission has not been granted.
     * User needs to grant permission to proceed.
     */
    object NoPermission : VeterinariansUiState()

    /**
     * State when GPS is disabled and no cached data is available.
     * User needs to enable GPS to see veterinarians.
     */
    object GpsDisabledNoCache : VeterinariansUiState()

    /**
     * State when GPS is disabled but cached data is available.
     * Shows previously fetched veterinarians with a warning message.
     *
     * @property veterinarians List of cached veterinarians.
     */
    data class GpsDisabledWithCache(
        val veterinarians: List<Veterinarian>
    ) : VeterinariansUiState()

    /**
     * State when location is available but no veterinarians were found nearby.
     */
    object EmptyList : VeterinariansUiState()

    /**
     * Success state with a list of nearby veterinarians.
     *
     * @property veterinarians List of veterinarians to display.
     */
    data class Success(
        val veterinarians: List<Veterinarian>
    ) : VeterinariansUiState()
}