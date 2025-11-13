package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
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
 * Uses AndroidViewModel approach from class lectures (no Hilt).
 *
 * Features:
 * - Load activities with animal and shelter details
 * - Schedule new activities
 * - Delete activities
 * - Loading states and error handling
 */
class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val activityRepository: ActivityRepository
    private val animalRepository: AnimalRepository
    private val shelterRepository: ShelterRepository

    init {
        val database = AppDatabase.getDatabase(application)
        activityRepository = ActivityRepository(database.activityDao())
        animalRepository = AnimalRepository(database.animalDao())
        shelterRepository = ShelterRepository(database.shelterDao())
    }

    // User ID for filtering activities
    private val _userId = MutableLiveData<Int>()

    /**
     * LiveData that combines Activity data with Animal and Shelter information.
     * Automatically updates when database changes.
     */
    val activitiesWithDetails: LiveData<List<ActivityWithAnimalAndShelter>> =
        _userId.switchMap { id ->
            activityRepository.getUpcomingActivitiesByUser(id)
                .switchMap { activityList ->
                    liveData {
                        val enriched = activityList.mapNotNull { act ->
                            val animal = animalRepository.getAnimalById(act.animalId) // ← Aqui o act.animalId precisa ser Int
                            val shelter = animal?.let { shelterRepository.getShelterById(it.shelterId) }
                            if (animal != null && shelter != null)
                                ActivityWithAnimalAndShelter(act, animal, shelter)
                            else null
                        }
                        emit(enriched)
                    }
                }
        }

    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Activity scheduled confirmation
    private val _activityScheduled = MutableLiveData(false)
    val activityScheduled: LiveData<Boolean> = _activityScheduled

    // Animal data for visit scheduling screen
    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    // Shelter data for visit scheduling screen
    private val _shelter = MutableLiveData<Shelter?>()
    val shelter: LiveData<Shelter?> = _shelter

    /**
     * Load animal and shelter data for the visit scheduling screen.
     * Used when user wants to schedule a visit to see an animal.
     */
//    fun loadAnimalAndShelter(animalId: Int) {
//        viewModelScope.launch {
//            try {
//                val animalData = animalRepository.getAnimalById(animalId)
//                _animal.value = animalData
//
//                animalData?.shelterId?.let { shelterId ->
//                    val shelterData = shelterRepository.getShelterById(shelterId)
//                    _shelter.value = shelterData
//                }
//            } catch (e: Exception) {
//                _animal.value = null
//                _shelter.value = null
//                _error.value = "Erro ao carregar dados: ${e.message}"
//            }
//        }
//    }

    // ====== MOCK ========
    fun loadAnimalAndShelter(animalId: Int) {
        viewModelScope.launch {
            try {
                println("🔍 DEBUG: animalId recebido = $animalId") //debug

                val animalData = animalRepository.getAnimalById(animalId)

                // TEMPORÁRIO: Se não encontrar na BD, usa dados mock
                if (animalData == null) {
                    val mockAnimal = getMockAnimal(animalId) // debug
                    println("🔍 DEBUG: usando mock = ${mockAnimal.name}") // debug
                    _animal.value = getMockAnimal(animalId)
                    _shelter.value = getMockShelter()
                } else {
                    _animal.value = animalData
                    animalData.shelterId.let { shelterId ->
                        val shelterData = shelterRepository.getShelterById(shelterId)
                        _shelter.value = shelterData
                    }
                }
            } catch (e: Exception) {
                val mockAnimal = getMockAnimal(animalId) // debug
                println("🔍 DEBUG: erro, usando mock = ${mockAnimal.name}") // debug

                _animal.value = getMockAnimal(animalId)
                _shelter.value = getMockShelter()
            }
        }
    }

    // TEMPORÁRIO: Funções auxiliares para mock
    private fun getMockAnimal(animalId: Int): Animal {
        return when (animalId) {
            1 -> Animal(
                id = 1,
                name = "Molly",
                breed = "Golden Retriever",
                species = "Dog",
                size = "Large",
                birthDate = "2020-01-01",
                imageUrl = listOf(),
                shelterId = 1
            )
            2 -> Animal(
                id = 2,
                name = "Max",
                breed = "Labrador",
                species = "Dog",
                size = "Medium",
                birthDate = "2021-03-15",
                imageUrl = listOf(),
                shelterId = 1
            )
            3 -> Animal(
                id = 3,
                name = "Luna",
                breed = "Siamese",
                species = "Cat",
                size = "Small",
                birthDate = "2022-06-10",
                imageUrl = listOf(),
                shelterId = 1
            )
            else -> Animal(
                id = animalId,
                name = "Animal Desconhecido",
                breed = "Desconhecido",
                species = "Dog",
                size = "Medium",
                birthDate = "2020-01-01",
                imageUrl = listOf(),
                shelterId = 1
            )
        }
    }

    // Mock
    private fun getMockShelter(): Shelter {
        return Shelter(
            id = 1,
            name = "Abrigo de Felgueiras",
            address = "Rua da Saúde, 1234 Santa Marta",
            contact = "253 000 000",
            email = "abrigo@example.com"
        )
    }

    // ====== /END MOCK ========

    /**
     * Set the user ID to filter activities.
     * This triggers the activitiesWithDetails LiveData to update.
     */
    fun loadActivitiesForUser(userId: Int) {
        _userId.value = userId
    }

    /**
     * Schedule a new activity (visit).
     * Shows loading state and handles errors.
     */
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

    /**
     * Delete an activity.
     */
    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            try {
                activityRepository.deleteActivity(activity)
            } catch (e: Exception) {
                _error.value = "Erro ao eliminar atividade: ${e.message}"
            }
        }
    }

    /**
     * Reset the activity scheduled flag.
     * Call this after navigating away from the scheduling screen.
     */
    fun resetActivityScheduled() {
        _activityScheduled.value = false
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
}