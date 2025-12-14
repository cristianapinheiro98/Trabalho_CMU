package pt.ipp.estg.trabalho_cmu.ui.screens.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.utils.StringHelper

/**
 * ViewModel responsible for managing favorites-related UI logic.
 *
 * It coordinates:
 * - Current user selection
 * - Favorites loaded from the repository (Room + Firestore)
 * - UI state for loading, success, and error feedback
 *
 * Uses an offline-first approach with explicit sync to Firestore.
 *
 * @param application Application context used to initialize dependencies.
 */
class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteRepository = DatabaseModule.provideFavoriteRepository(application)

    /**
     * Backing property for the current UI state of favorites operations.
     */
    private val _uiState = MutableLiveData<FavoriteUiState>(FavoriteUiState.Initial)
    /**
     * Public read-only UI state observed by the UI layer.
     */
    val uiState: LiveData<FavoriteUiState> = _uiState

    /**
     * Backing property holding the currently selected user ID.
     */
    private val _currentUserId = MutableLiveData<String?>()

    /**
     * Exposes the current user ID as LiveData for observation.
     */
    val currentUserId: LiveData<String?> = _currentUserId


    /**
     * Application context shortcut, useful for accessing string resources.
     */
    val ctx = getApplication<Application>()


    /**
     * Reactive list of favorites for the current user.
     *
     * When [_currentUserId] changes:
     * - If null or empty, emits an empty list.
     * - Otherwise, subscribes to Room favorites for that user.
     */
    val favorites: LiveData<List<Favorite>> = _currentUserId.switchMap { userId ->
        if (userId.isNullOrEmpty()) {
            MutableLiveData(emptyList())
        } else {
            favoriteRepository.getFavoritesByUserLive(userId)
        }
    }

    /**
     * Sets the currently active user ID for this ViewModel.
     *
     * If the ID changes, the favorites LiveData source will be updated.
     *
     * @param userId New user ID, or null to clear the current user.
     */
    fun setCurrentUser(userId: String?) {
        if (_currentUserId.value != userId) {
            _currentUserId.value = userId
        }
    }

    /**
     * Synchronizes favorites from Firebase into the local Room database
     * for the provided user ID.
     *
     * Updates the UI state to Loading during the process and
     * resets to Initial or Error after completion.
     *
     * @param userId ID of the user whose favorites should be synchronized.
     */
    fun syncFavorites(userId: String) = viewModelScope.launch {
        _uiState.value = FavoriteUiState.Loading
        try {
            favoriteRepository.syncFavorites(userId)
            _uiState.value = FavoriteUiState.Initial
        } catch (e: Exception) {
            _uiState.value = FavoriteUiState.Error(StringHelper.getString(ctx, R.string.error_favorite_sync))
        }
    }

    // ==============================
    // ADD FAVORITE
    // ==============================

    /**
     * Adds a new favorite for the given user and animal.
     *
     * Flow:
     * - Sets UI state to Loading.
     * - Builds a Favorite instance with the provided IDs.
     * - Calls repository to add and sync favorites.
     * - On success, publishes [FavoriteUiState.FavoriteAdded].
     * - On failure, publishes [FavoriteUiState.Error].
     *
     * @param userId ID of the user adding the favorite.
     * @param animalId ID of the animal to be added to favorites.
     */
    fun addFavorite(userId: String, animalId: String) = viewModelScope.launch {
        _uiState.value = FavoriteUiState.Loading
        try {
            val favorite = Favorite(
                userId = userId,
                animalId = animalId
            )
            favoriteRepository.addFavorite(userId, favorite)
            favoriteRepository.syncFavorites(userId)
            _uiState.value = FavoriteUiState.FavoriteAdded(favorite)
        } catch (e: Exception) {
            _uiState.value = FavoriteUiState.Error(StringHelper.getString(ctx, R.string.error_favorite_add))
        }
    }


    /**
     * Removes a favorite relation between a user and an animal.
     *
     * Flow:
     * - Sets UI state to Loading.
     * - Delegates removal to the repository.
     * - On success, sets [FavoriteUiState.FavoriteRemoved].
     * - On failure, sets [FavoriteUiState.Error].
     *
     * @param userId ID of the user who owns the favorite.
     * @param animalId ID of the animal to be removed from favorites.
     */

    fun removeFavorite(userId: String, animalId: String) = viewModelScope.launch {
        _uiState.value = FavoriteUiState.Loading
        try {
            favoriteRepository.removeFavorite(userId, animalId)
            _uiState.value = FavoriteUiState.FavoriteRemoved
        } catch (e: Exception) {
            _uiState.value = FavoriteUiState.Error(StringHelper.getString(ctx, R.string.error_favorite_remove))
        }
    }

    /**
     * Resets the UI state to [FavoriteUiState.Initial].
     *
     * Typically called after the UI has already reacted to
     * a success or error state and should be cleared.
     */
    fun resetState() {
        _uiState.value = FavoriteUiState.Initial
    }
}
