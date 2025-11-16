package pt.ipp.estg.trabalho_cmu.ui.screens.Veterinarians

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.repository.VeterinarianRepository

class VeterinariansViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VeterinarianRepository

    private val _uiState = MutableStateFlow<VeterinariansUiState>(
        VeterinariansUiState.LoadingLocation
    )
    val uiState: StateFlow<VeterinariansUiState> = _uiState.asStateFlow()

    init {
        val veterinarianDao = AppDatabase.getDatabase(application).veterinarianDao()
        repository = VeterinarianRepository(veterinarianDao)
    }

    // Location permission denied
    fun onPermissionDenied() {
        _uiState.value = VeterinariansUiState.NoPermission
    }

    // Location permission is granted
    fun onPermissionGranted() {
        _uiState.value = VeterinariansUiState.LoadingLocation
    }

    // When the GPS is disabled
    fun onGpsDisabled() {
        viewModelScope.launch {
            // Verifica se tem cache
            val cachedVets = repository.getAllVeterinarians().value ?: emptyList()

            _uiState.value = if (cachedVets.isEmpty()) {
                VeterinariansUiState.GpsDisabledNoCache
            } else {
                VeterinariansUiState.GpsDisabledWithCache(cachedVets)
            }
        }
    }

    // When location is obtained
    fun onLocationReceived(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            // Verify cache and update if necessary
            val cachedVets = repository.getAllVeterinarians().value ?: emptyList()

            if (!repository.isCacheValid(cachedVets)) {
                // Expired cache -> refresh from API
                repository.refreshVeterinariansFromAPI(latitude, longitude)
            }

            // Observe list changes
            observeVeterinarians()
        }
    }

    // Force manual refresh
    fun refreshVeterinarians(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = VeterinariansUiState.LoadingLocation
            repository.refreshVeterinariansFromAPI(latitude, longitude)
            observeVeterinarians()
        }
    }

    // Observe verterinarian list
    private fun observeVeterinarians() {
        viewModelScope.launch {
            repository.getAllVeterinarians().observeForever { veterinarians ->
                _uiState.value = if (veterinarians.isEmpty()) {
                    VeterinariansUiState.EmptyList
                } else {
                    VeterinariansUiState.Success(veterinarians)
                }
            }
        }
    }
}