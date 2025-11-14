package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.repository.ActivityRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository

data class ActivityWithAnimalAndShelter(
    val activity: Activity,
    val animal: Animal,
    val shelter: Shelter
)

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(application)
    }

    private val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao())
    }

    private val animalRepository: AnimalRepository by lazy {
        AnimalRepository(database.animalDao(), FirebaseFirestore.getInstance())
    }

    private val shelterRepository: ShelterRepository by lazy {
        ShelterRepository(database.shelterDao())
    }

    private val _userId = MutableLiveData<Int>()

    val activitiesWithDetails: LiveData<List<ActivityWithAnimalAndShelter>> =
        _userId.switchMap { userId ->
            activityRepository.getUpcomingActivitiesByUser(userId).switchMap { activityList ->
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
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _activityScheduled = MutableLiveData(false)
    val activityScheduled: LiveData<Boolean> = _activityScheduled

    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    private val _shelter = MutableLiveData<Shelter?>()
    val shelter: LiveData<Shelter?> = _shelter


    fun loadAnimalAndShelter(animalId: Int) {
        viewModelScope.launch {
            try {
                val a = animalRepository.getAnimalById(animalId)
                _animal.value = a

                val s = a?.let { shelterRepository.getShelterById(it.shelterId) }
                _shelter.value = s

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
                _animal.value = null
                _shelter.value = null
            }
        }
    }

    fun loadActivitiesForUser(userId: Int) {
        _userId.value = userId
    }

    fun scheduleActivity(activity: Activity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                activityRepository.addActivity(activity)
                _activityScheduled.value = true
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

    fun clearError() {
        _error.value = null
    }
}
