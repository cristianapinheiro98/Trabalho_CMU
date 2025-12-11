package pt.ipp.estg.trabalho_cmu.ui.screens.Activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.repository.ActivityRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel for Activities History Screen.
 *
 * Manages the state of the activities history, including:
 * - Loading and categorizing activities (ongoing, upcoming, past)
 * - Syncing activities from Firebase
 * - Handling offline/online states
 * - Deleting activities
 *
 * Follows MVVM pattern with a single LiveData<UiState> as the source of truth.
 */
class ActivitiesHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val activityRepository = DatabaseModule.provideActivityRepository(application)
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)
    private val shelterRepository = DatabaseModule.provideShelterRepository(application)

    private val _uiState = MutableLiveData<ActivitiesHistoryUiState>(ActivitiesHistoryUiState.Initial)
    val uiState: LiveData<ActivitiesHistoryUiState> = _uiState

    /**
     * Loads all activities for a user and categorizes them.
     *
     * If online: syncs from Firebase and shows all activities.
     * If offline: shows cached activities with a warning.
     *
     * @param userId The ID of the current user.
     */
    fun loadActivities(userId: String) {
        viewModelScope.launch {
            _uiState.value = ActivitiesHistoryUiState.Loading

            val isOnline = NetworkUtils.isConnected()

            try {
                if (isOnline) {
                    activityRepository.syncActivities(userId)
                }

                activityRepository.getAllActivitiesByUser(userId).observeForever { activities ->
                    viewModelScope.launch {
                        enrichActivities(activities, isOnline)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ActivitiesHistoryUiState.Error(e.message ?: "Error loading activities")
            }
        }
    }

    /**
     * Enriches activities with animal and shelter data.
     *
     * @param activities List of activities to enrich.
     * @param isOnline Whether the device is online.
     */
    private suspend fun enrichActivities(activities: List<Activity>, isOnline: Boolean) {
        if (activities.isEmpty()) {
            _uiState.value = ActivitiesHistoryUiState.Empty
            return
        }

        val enriched = activities.mapNotNull { activity ->
            val animal = animalRepository.getAnimalById(activity.animalId) ?: return@mapNotNull null
            val shelter = shelterRepository.getShelterById(animal.shelterId) ?: return@mapNotNull null
            ActivityWithDetails(activity, animal, shelter)
        }

        _uiState.value = if (isOnline) {
            ActivitiesHistoryUiState.OnlineSuccess(enriched)
        } else {
            ActivitiesHistoryUiState.OfflineSuccess(enriched)
        }
    }

    /**
     * Deletes an activity and reloads the list.
     *
     * @param activityId The ID of the activity to delete.
     * @param userId The ID of the current user.
     */
    fun deleteActivity(activityId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = ActivitiesHistoryUiState.Loading

            activityRepository.deleteActivity(activityId)
                .onSuccess {
                    loadActivities(userId)
                }
                .onFailure { e ->
                    _uiState.value = ActivitiesHistoryUiState.Error(e.message ?: "Error deleting activity")
                }
        }
    }
}