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

    /**
     * Repository handling animal data (Room + Firebase).
     */
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)

    /**
     * Repository handling shelter data (Room + Firebase).
     */
    private val shelterRepository = DatabaseModule.provideShelterRepository(application)


    /**
     * Backing field for the animal-related UI state.
     */
    private val _uiState = MutableLiveData<AnimalUiState>(AnimalUiState.Initial)

    /**
     * Public observable UI state used by the UI to react to operations.
     */
    val uiState: LiveData<AnimalUiState> = _uiState

    /**
     * Backing field for the current filtered list of animals.
     * This holds the result of filter/sort/search operations.
     */
    private val _filteredAnimals = MutableLiveData<List<Animal>>()

    /**
     * Public observable filtered animals list.
     */
    val filteredAnimals: LiveData<List<Animal>> = _filteredAnimals

    /**
     * Backing field for the currently selected animal.
     */
    private val _selectedAnimal = MutableLiveData<Animal?>()

    /**
     * Public observable selected animal to be displayed in detail screens.
     */
    val selectedAnimal: LiveData<Animal?> = _selectedAnimal

    /**
     * Backing field for the full list of animals.
     */
    private val _animals = MutableLiveData<List<Animal>>()

    /**
     * Public observable full list of animals loaded into memory.
     */
    val animals: LiveData<List<Animal>> = _animals

    /**
     * Backing field for the full list of shelters.
     */
    private val _shelters = MutableLiveData<List<Shelter>>()

    /**
     * Public observable list of shelters, used for UI and mapping.
     */
    val shelters: LiveData<List<Shelter>> = _shelters

    /**
     * Application context shortcut, useful for accessing string resources.
     */
    val ctx = getApplication<Application>()



    init {
        loadShelters()
        loadAnimals()
    }

    /** Loads shelters from Firebase if online, otherwise from Room. */
    private fun loadShelters() {
        viewModelScope.launch {
            try {
                shelterRepository.syncShelters()
                _shelters.value = shelterRepository.getAllSheltersList()
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
                _animals.value = animalRepository.getAllAnimalsList()
            } catch (e: Exception) {
                _animals.value = animalRepository.getAllAnimalsList()
            }
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
