package pt.ipp.estg.trabalho_cmu.ui.screens.Veterinarians

import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian

sealed class VeterinariansUiState {
    // Loading location
    object LoadingLocation : VeterinariansUiState()

    // Without location permission
    object NoPermission : VeterinariansUiState()

    // GPS off + no cache
    object GpsDisabledNoCache : VeterinariansUiState()

    // GPS off + with cache
    data class GpsDisabledWithCache(
        val veterinarians: List<Veterinarian>
    ) : VeterinariansUiState()

    // Location OK + empty list
    object EmptyList : VeterinariansUiState()

    // Location OK + list with data
    data class Success(
        val veterinarians: List<Veterinarian>
    ) : VeterinariansUiState()
}