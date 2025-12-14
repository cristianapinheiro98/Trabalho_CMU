package pt.ipp.estg.trabalho_cmu.ui.screens.veterinarians

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.repository.VeterinarianRepository

/**
 * ViewModel for the Veterinarians Screen.
 *
 * Manages the state of the veterinarians list, including:
 * - Handling location permission states
 * - Managing GPS availability
 * - Fetching and caching veterinarian data
 * - Coordinating between API and local cache
 */
class VeterinariansViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VeterinarianRepository

    private val _uiState = MutableLiveData<VeterinariansUiState>(VeterinariansUiState.LoadingLocation)
    val uiState: LiveData<VeterinariansUiState> = _uiState

    init {
        val veterinarianDao = AppDatabase.getDatabase(application).veterinarianDao()
        repository = VeterinarianRepository(veterinarianDao)
    }

    /**
     * Called when location permission is denied by the user.
     * Updates UI state to show permission request.
     */
    fun onPermissionDenied() {
        _uiState.value = VeterinariansUiState.NoPermission
    }

    /**
     * Called when location permission is granted.
     * Sets loading state while waiting for location.
     */
    fun onPermissionGranted() {
        _uiState.value = VeterinariansUiState.LoadingLocation
    }

    /**
     * Called when GPS is disabled or location is unavailable.
     * Shows cached data if available, otherwise prompts user to enable GPS.
     */
    fun onGpsDisabled() {
        viewModelScope.launch {
            val cachedVets = repository.getCachedVeterinarians()

            _uiState.value = if (cachedVets.isEmpty()) {
                VeterinariansUiState.GpsDisabledNoCache
            } else {
                VeterinariansUiState.GpsDisabledWithCache(cachedVets)
            }
        }
    }

    /**
     * Called when user location is successfully obtained.
     * Checks cache validity and refreshes from API if needed.
     *
     * @param latitude User's current latitude.
     * @param longitude User's current longitude.
     */
    fun onLocationReceived(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val cachedVets = repository.getCachedVeterinarians()

            if (!repository.isCacheValid(cachedVets)) {
                repository.refreshVeterinariansFromAPI(latitude, longitude)
            }

            loadVeterinarians()
        }
    }

    /**
     * Forces a manual refresh of the veterinarians list from the API.
     *
     * @param latitude User's current latitude.
     * @param longitude User's current longitude.
     */
    fun refreshVeterinarians(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = VeterinariansUiState.LoadingLocation

            repository.refreshVeterinariansFromAPI(latitude, longitude)
            loadVeterinarians()
        }
    }

    /**
     * Loads veterinarians from the local cache and updates UI state.
     */
    private suspend fun loadVeterinarians() {
        val veterinarians = repository.getCachedVeterinarians()

        _uiState.value = if (veterinarians.isEmpty()) {
            VeterinariansUiState.EmptyList
        } else {
            VeterinariansUiState.Success(veterinarians)
        }
    }
}