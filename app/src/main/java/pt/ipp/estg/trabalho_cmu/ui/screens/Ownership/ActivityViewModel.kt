package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.repository.ActivityRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository
import javax.inject.Inject

data class ActivityWithAnimalAndShelter(
    val activity: Activity,
    val animal: Animal,
    val shelter: Shelter
)

@HiltViewModel
open class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val animalRepository: AnimalRepository,
    private val shelterRepository: ShelterRepository
) : ViewModel() {

    private val _userId = MutableLiveData<String>()

    open val activitiesWithDetails: LiveData<List<ActivityWithAnimalAndShelter>> =
        _userId.switchMap { id ->
            activityRepository.getUpcomingActivitiesByUser(id)
                .switchMap { activityList ->
                    liveData {
                        val enriched = activityList.mapNotNull { act ->
                            val animal = animalRepository.getAnimalById(act.animalId)
                            val shelter = animal?.let { shelterRepository.getShelterById(it.shelterId) }
                            if (animal != null && shelter != null)
                                ActivityWithAnimalAndShelter(act, animal, shelter)
                            else null
                        }
                        emit(enriched)
                    }
                }
        }


    private val _isLoading = MutableLiveData(false)
    open val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    open val error: LiveData<String?> = _error

    private val _activityScheduled = MutableLiveData(false)
    open val activityScheduled: LiveData<Boolean> = _activityScheduled
    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    private val _shelter = MutableLiveData<Shelter?>()
    val shelter: LiveData<Shelter?> = _shelter

    fun loadAnimalAndShelter(animalId: String) {
        viewModelScope.launch {
            try {
                val animalData = animalRepository.getAnimalById(animalId)
                _animal.value = animalData

                animalData?.shelterId?.let { shelterId ->
                    val shelterData = shelterRepository.getShelterById(shelterId)
                    _shelter.value = shelterData
                }
            } catch (e: Exception) {
                // Handle error if needed
                _animal.value = null
                _shelter.value = null
            }
        }
    }

    fun loadActivitiesForUser(userId: String) {
        _userId.value = userId
    }

    fun scheduleActivity(activity: Activity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                activityRepository.addActivity(activity)
                _activityScheduled.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao agendar atividade: ${e.message}"
                _activityScheduled.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            try {
                activityRepository.deleteActivity(activity)
            } catch (e: Exception) {
                _error.value = "Erro ao eliminar atividade: ${e.message}"
            }
        }
    }

    fun resetActivityScheduled() {
        _activityScheduled.value = false
    }
}
