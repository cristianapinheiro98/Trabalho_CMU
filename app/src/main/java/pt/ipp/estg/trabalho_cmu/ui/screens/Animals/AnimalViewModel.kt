package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule

private const val TAG = "AnimalViewModel"

/**
 * ViewModel responsible for:
 * - Loading animals and shelters from Room or Firebase
 * - Synchronizing data when online
 * - Filtering, sorting and searching animals
 * - Creating animals and exposing UI state changes
 *
 * This ViewModel extends AndroidViewModel so the Application context
 * can be accessed if needed (e.g., for string resources inside repositories).
 */
class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val animalRepository = DatabaseModule.provideAnimalRepository(application)
    private val shelterRepository = DatabaseModule.provideShelterRepository(application)

    private val _uiState = MutableLiveData<AnimalUiState>(AnimalUiState.Initial)
    val uiState: LiveData<AnimalUiState> = _uiState

    private val _filteredAnimals = MutableLiveData<List<Animal>>()
    val filteredAnimals: LiveData<List<Animal>> = _filteredAnimals

    private val _selectedAnimal = MutableLiveData<Animal?>()
    val selectedAnimal: LiveData<Animal?> = _selectedAnimal

    private val _animals = MutableLiveData<List<Animal>>()
    val animals: LiveData<List<Animal>> = _animals

    private val _shelters = MutableLiveData<List<Shelter>>()
    val shelters: LiveData<List<Shelter>> = _shelters


    init {
        loadShelters()
        loadAnimals()
    }

    /** Loads shelters from Firebase if online, otherwise from Room. */
    private fun loadShelters() {
        viewModelScope.launch {
            try {
                shelterRepository.syncShelters()
                _shelters.value = shelterRepository.getSheltersFromRoom()
            } catch (e: Exception) {
                _shelters.value = shelterRepository.getAllSheltersList()
            }
        }
    }

    /** Loads animals using an online-then-local strategy. */
    private fun loadAnimals() {
        viewModelScope.launch {
            try {
                animalRepository.syncAnimals() // update Room
                _animals.value = animalRepository.getAnimalsFromRoom()
            } catch (e: Exception) {
                _animals.value = animalRepository.getAllAnimalsList()
            }
        }
    }

    // ================= CREATE ANIMAL =================

    /**
     * Creates an animal in Firebase and then resynchronizes local data.
     */
    fun createAnimal(animal: Animal) = viewModelScope.launch {
        Log.d(TAG, "createAnimal: ${animal.id}")
        _uiState.value = AnimalUiState.Loading

        animalRepository.createAnimal(animal)
            .onSuccess {
                Log.d(TAG, "Animal criado com sucesso, a ressincronizar...")
                animalRepository.syncAnimals()
                _uiState.value = AnimalUiState.AnimalCreated(it)
            }
            .onFailure { exception ->
                Log.e(TAG, "Erro ao criar animal", exception)
                _uiState.value = AnimalUiState.Error(
                    exception.message ?: "R.string.error_creating_animal"
                )
            }
    }

    // ================= FILTER & SORT =================

    fun filterBySpecies(species: String) = viewModelScope.launch {
        try {
            _filteredAnimals.value = animalRepository.filterBySpecies(species)
        } catch (e: Exception) {
            _filteredAnimals.value = emptyList()
        }
    }

    fun filterBySize(size: String) = viewModelScope.launch {
        try {
            _filteredAnimals.value = animalRepository.filterBySize(size)
        } catch (_: Exception) {
            _filteredAnimals.value = emptyList()
        }
    }

    fun sortByNameAsc() = viewModelScope.launch {
        try {
            _filteredAnimals.value = animalRepository.sortByNameAsc()
        } catch (_: Exception) {
            _filteredAnimals.value = emptyList()
        }
    }

    fun sortByNameDesc() = viewModelScope.launch {
        try {
            _filteredAnimals.value = animalRepository.sortByNameDesc()
        } catch (_: Exception) {
            _filteredAnimals.value = emptyList()
        }
    }

    fun sortByAgeAsc() = viewModelScope.launch {
        try {
            _filteredAnimals.value = animalRepository.sortByAgeAsc()
        } catch (_: Exception) {
            _filteredAnimals.value = emptyList()
        }
    }

    fun sortByAgeDesc() = viewModelScope.launch {
        try {
            _filteredAnimals.value = animalRepository.sortByAgeDesc()
        } catch (_: Exception) {
            _filteredAnimals.value = emptyList()
        }
    }

    fun searchAnimals(query: String) = viewModelScope.launch {
        try {
            _filteredAnimals.value = animalRepository.searchAnimals(query)
        } catch (_: Exception) {
            _filteredAnimals.value = emptyList()
        }
    }

    fun clearFilters() {
        _filteredAnimals.value = emptyList()
    }

    // ================= SELECT ANIMAL =================

    fun selectAnimal(animalId: String) = viewModelScope.launch {
        try {
            _selectedAnimal.value = animalRepository.getAnimalById(animalId)
        } catch (_: Exception) {
            _selectedAnimal.value = null
        }
    }

    fun refreshAnimals() {
        viewModelScope.launch { animalRepository.syncAnimals() }
    }

    fun selectAnimal(animal: Animal) {
        _selectedAnimal.value = animal
    }

    fun clearSelectedAnimal() {
        _selectedAnimal.value = null
    }
}
