package pt.ipp.estg.trabalho_cmu.ui.screens.User

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteRepository = DatabaseModule.provideFavoriteRepository(application)

    // ------------------------------
    // UI STATE
    // ------------------------------
    private val _uiState = MutableLiveData<FavoriteUiState>(FavoriteUiState.Initial)
    val uiState: LiveData<FavoriteUiState> = _uiState

    // ------------------------------
    // USER ID ATUAL
    // ------------------------------
    private val _currentUserId = MutableLiveData<String?>()
    val currentUserId: LiveData<String?> = _currentUserId

    // ------------------------------
    // LISTA DE FAVORITOS (LiveData do Room)
    // ------------------------------
    val favorites: LiveData<List<Favorite>> = _currentUserId.switchMap { userId ->
        if (userId.isNullOrEmpty()) {
            MutableLiveData(emptyList())
        } else {
            favoriteRepository.getFavoritesByUserLive(userId)
        }
    }

    // ==============================
    // DEFINIR USER ATUAL
    // ==============================

    fun setCurrentUser(userId: String?) {
        if (_currentUserId.value != userId) {
            _currentUserId.value = userId
        }
    }

    // ==============================
    // SINCRONIZAR FIREBASE -> ROOM
    // ==============================

    fun syncFavorites(userId: String) = viewModelScope.launch {
        _uiState.value = FavoriteUiState.Loading
        try {
            favoriteRepository.syncFavorites(userId)
            _uiState.value = FavoriteUiState.Initial
        } catch (e: Exception) {
            _uiState.value = FavoriteUiState.Error("Erro a sincronizar favoritos.")
        }
    }

    // ==============================
    // ADICIONAR FAVORITO
    // ==============================

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
            _uiState.value = FavoriteUiState.Error("Erro ao adicionar favorito.")
        }
    }

    // ==============================
    // REMOVER FAVORITO
    // ==============================

    fun removeFavorite(userId: String, animalId: String) = viewModelScope.launch {
        _uiState.value = FavoriteUiState.Loading
        try {
            favoriteRepository.removeFavorite(userId, animalId)
            _uiState.value = FavoriteUiState.FavoriteRemoved
        } catch (e: Exception) {
            _uiState.value = FavoriteUiState.Error("Erro ao remover favorito.")
        }
    }

    // ==============================
    // RESET DO ESTADO UI
    // ==============================

    fun resetState() {
        _uiState.value = FavoriteUiState.Initial
    }
}
