package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
data class ActivityWithAnimalAndShelter(
    val activity: Activity,
    val animal: Animal,
    val shelter: Shelter
)

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val activityRepository = DatabaseModule.provideActivityRepository(application)
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)
    private val shelterRepository = DatabaseModule.provideShelterRepository(application)

    // --- UI STATE ---
    private val _uiState = MutableLiveData<ActivityUiState>(ActivityUiState.Initial)
    val uiState: LiveData<ActivityUiState> = _uiState

    // --- HELPERS MANUAIS ---
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _activityScheduled = MutableLiveData(false)
    val activityScheduled: LiveData<Boolean> = _activityScheduled

    // --- DADOS HISTÓRICO (Mediator) ---
    private val _activitiesSource = MutableLiveData<List<Activity>>()
    val activitiesWithDetails = MediatorLiveData<List<ActivityWithAnimalAndShelter>>()

    // --- DETALHES PARA AGENDAMENTO (1 Animal + 1 Shelter) ---
    private val _animal = MutableLiveData<Animal?>(null)
    val animal: LiveData<Animal?> = _animal

    private val _shelter = MutableLiveData<Shelter?>(null)
    val shelter: LiveData<Shelter?> = _shelter

    // --- FORM FIELDS ---
    val pickupDate = MutableLiveData("")
    val pickupTime = MutableLiveData("09:00")
    val deliveryDate = MutableLiveData("")
    val deliveryTime = MutableLiveData("18:00")

    init {
        // Configura Mediator para lista de histórico
        activitiesWithDetails.addSource(_activitiesSource) { activities ->
            loadDetailsForActivities(activities)
        }
    }

    private fun setUiState(state: ActivityUiState) {
        _uiState.value = state
        _isLoading.value = state is ActivityUiState.Loading
        _error.value = (state as? ActivityUiState.Error)?.message
        _activityScheduled.value = state is ActivityUiState.ActivityScheduled
    }

    // =========================================================================
    //  CARREGAR HISTÓRICO
    // =========================================================================
    fun loadActivitiesForUser(userId: String) {
        viewModelScope.launch {
            activityRepository.syncActivities(userId)
        }
        val roomLiveData = activityRepository.getAllActivitiesByUser(userId)

        activitiesWithDetails.removeSource(roomLiveData) // Evitar duplicados
        activitiesWithDetails.addSource(roomLiveData) { activities ->
            loadDetailsForActivities(activities)
        }
    }

    private fun loadDetailsForActivities(activities: List<Activity>) = viewModelScope.launch {
        val list = mutableListOf<ActivityWithAnimalAndShelter>()
        for (act in activities) {
            val anim = animalRepository.getAnimalById(act.animalId)
            if (anim != null) {
                val shelt = shelterRepository.getShelterById(anim.shelterId)
                if (shelt != null) {
                    list.add(ActivityWithAnimalAndShelter(act, anim, shelt))
                }
            }
        }
        activitiesWithDetails.value = list
    }

    // =========================================================================
    //  CARREGAR DADOS PARA O ECRÃ DE AGENDAMENTO
    // =========================================================================
    fun loadAnimalAndShelter(animalId: String) = viewModelScope.launch {
        setUiState(ActivityUiState.Loading)
        try {
            val anim = animalRepository.getAnimalById(animalId)
            _animal.value = anim

            if (anim != null) {
                _shelter.value = shelterRepository.getShelterById(anim.shelterId)
            }
            setUiState(ActivityUiState.Success)
        } catch (e: Exception) {
            setUiState(ActivityUiState.Error("Erro ao carregar dados: ${e.message}"))
        }
    }

    // =========================================================================
    //  CRIAR ATIVIDADE
    // =========================================================================
    fun scheduleActivity(userId: String, animalId: String, selectedDates: List<String>) = viewModelScope.launch {
        val pTime = pickupTime.value ?: "09:00"
        val dTime = deliveryTime.value ?: "18:00"

        if (selectedDates.isEmpty()) {
            setUiState(ActivityUiState.Error("Selecione pelo menos uma data."))
            return@launch
        }

        val sorted = selectedDates.sorted()

        val activity = Activity(
            id = "", userId = userId, animalId = animalId,
            pickupDate = sorted.first(), pickupTime = pTime,
            deliveryDate = sorted.last(), deliveryTime = dTime
        )

        setUiState(ActivityUiState.Loading)
        activityRepository.createActivity(activity)
            .onSuccess {
                setUiState(ActivityUiState.ActivityScheduled(it))
                resetFields()
            }
            .onFailure {
                setUiState(ActivityUiState.Error(it.message ?: "Erro ao agendar."))
            }
    }

    fun deleteActivity(activityId: String) = viewModelScope.launch {
        setUiState(ActivityUiState.Loading)
        activityRepository.deleteActivity(activityId)
            .onSuccess { setUiState(ActivityUiState.ActivityDeleted) }
            .onFailure { setUiState(ActivityUiState.Error(it.message ?: "Erro ao apagar.")) }
    }

    private fun resetFields() {
        pickupDate.value = ""
        pickupTime.value = "09:00"
        deliveryDate.value = ""
        deliveryTime.value = "18:00"
    }

    fun resetActivityScheduled() { setUiState(ActivityUiState.Initial) }
    fun resetState() { setUiState(ActivityUiState.Initial) }
}