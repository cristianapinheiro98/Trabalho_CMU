package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.data.repository.ActivityRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.ShelterRepository

/**
 * Data class to combine Activity with its Animal and Shelter information
 */
data class ActivityWithAnimalAndShelter(
    val activity: Activity,
    val animal: Animal,
    val shelter: Shelter
)

/**
 * ViewModel for managing Activities with enriched data (Animal + Shelter).
 */
class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(application)
    }

    private val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao())
    }

    private val animalRepository: AnimalRepository by lazy {
        AnimalRepository(database.animalDao())
    }

    private val shelterRepository = ShelterRepository(
        database.shelterDao()
    )

    private val _userId = MutableLiveData<Int>()

    val activitiesWithDetails: LiveData<List<ActivityWithAnimalAndShelter>> =
        _userId.switchMap { userId ->
            activityRepository.getUpcomingActivitiesByUser(userId).switchMap { activityList ->
                liveData {
                    val enriched = activityList.mapNotNull { act ->
                        val animal = animalRepository.getAnimalById(act.animalId)
                        val shelter = animal?.let { shelterRepository.getShelterByFirebaseUid(it.shelterFirebaseUid) }
                        if (animal != null && shelter != null)
                            ActivityWithAnimalAndShelter(act, animal, shelter)
                        else null
                    }
                    emit(enriched)
                }
            }
        }

    // ---- UI STATES ----
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


    /*fun loadAnimalAndShelter(animalId: Int) {
        viewModelScope.launch {
            try {
                val animal = animalRepository.getAnimalById(animalId)
                _animal.value = animal

                val shelter = animal?.let { shelterRepository.getShelterById(it.shelterId) }
                _shelter.value = shelter

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
                _animal.value = null
                _shelter.value = null
            }
        }
    }*/

    // ====== MOCK ========
    fun loadAnimalAndShelter(animalId: Int) {
        val ctx = getApplication<Application>()

        viewModelScope.launch {
            try {
                val animal = animalRepository.getAnimalById(animalId)
                _animal.value = animal

                val shelter = animal?.let { shelterRepository.getShelterByFirebaseUid(it.shelterFirebaseUid) }
                _shelter.value = shelter

                _error.value = null

                val animalData = animalRepository.getAnimalById(animalId)

                if (animalData == null) {
                    _animal.value = getMockAnimal(animalId)
                    _shelter.value = getMockShelter()
                } else {
                    _animal.value = animalData
                    animalData.shelterFirebaseUid.let { shelterFirebaseUid ->
                        val shelterData = shelterRepository.getShelterByFirebaseUid(shelterFirebaseUid)
                        _shelter.value = shelterData
                    }
                }
            } catch (e: Exception) {
                _error.value = ctx.getString(R.string.error_loading_animal_shelter) + " ${e.message}"
                _animal.value = getMockAnimal(animalId)
                _shelter.value = getMockShelter()
            }
        }
    }

    // Auxiliary mock functions
    private fun getMockAnimal(animalId: Int): Animal {
        return when (animalId) {
            1 -> Animal(
                id = 1,
                name = "Molly",
                breed = "Golden Retriever",
                species = "Dog",
                size = "Large",
                birthDate = "2020-01-01",
                imageUrls = listOf(),
                shelterFirebaseUid = "1",
                status = AnimalStatus.AVAILABLE,
                createdAt = System.currentTimeMillis(),
                description = "cãozinho fofo"
            )
            2 -> Animal(
                id = 2,
                name = "Max",
                breed = "Labrador",
                species = "Dog",
                size = "Medium",
                birthDate = "2021-03-15",
                imageUrls = listOf(),
                shelterFirebaseUid = "1",
                status = AnimalStatus.AVAILABLE,
                createdAt = System.currentTimeMillis(),
                description = "cãozinho fofo"
            )
            3 -> Animal(
                id = 3,
                name = "Luna",
                breed = "Siamese",
                species = "Cat",
                size = "Small",
                birthDate = "2022-06-10",
                imageUrls = listOf(),
                shelterFirebaseUid = "1",
                status = AnimalStatus.AVAILABLE,
                createdAt = System.currentTimeMillis(),
                description = "cãozinho fofo"
            )
            else -> Animal(
                id = animalId,
                name = "Animal Desconhecido",
                breed = "Desconhecido",
                species = "Dog",
                size = "Medium",
                birthDate = "2020-01-01",
                imageUrls = listOf(),
                shelterFirebaseUid = "1",
                status = AnimalStatus.AVAILABLE,
                createdAt = System.currentTimeMillis(),
                description = "cãozinho fofo"
            )
        }
    }

    private fun getMockShelter(): Shelter {
        return Shelter(
            id = 1,
            firebaseUid = "1",
            name = "Abrigo de Felgueiras",
            address = "Rua da Saúde, 1234 Santa Marta",
            phone = "253 000 000",
            email = "abrigo@example.com",
            password = ""
        )
    }
    // ====== /END MOCK ========

    fun loadActivitiesForUser(userId: Int) {
        _userId.value = userId
    }

    fun scheduleActivity(activity: Activity) {
        val ctx = getApplication<Application>()

        viewModelScope.launch {
            try {
                _isLoading.value = true
                activityRepository.addActivity(activity)
                _activityScheduled.value = true
            } catch (e: Exception) {
                _error.value =
                    ctx.getString(R.string.error_scheduling_activity) + " ${e.message}"
                _activityScheduled.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteActivity(activity: Activity) {
        val ctx = getApplication<Application>()

        viewModelScope.launch {
            try {
                activityRepository.deleteActivity(activity)
            } catch (e: Exception) {
                _error.value =
                    ctx.getString(R.string.error_deleting_activity) + " ${e.message}"
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
