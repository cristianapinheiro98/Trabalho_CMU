package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository

/**
 * ViewModel responsible for all animal-related operations:
 * - Fetching animals (from Room and Firebase)
 * - Managing filters (species, size, age, name)
 * - Searching
 * - Sorting
 * - Selecting animals for detail screen
 * - Handling favorites
 *
 * This ViewModel exposes immutable LiveData to the UI layer and interacts
 * with the AnimalRepository which handles data persistence and synchronization.
 */
class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    /** Repository providing access to Room DAO and Firebase synchronization */
    private val repository: AnimalRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        AnimalRepository(db.animalDao(), FirebaseFirestore.getInstance())
    }

    /** Live list of all animals stored locally */
    val animals: LiveData<List<Animal>> = repository.getAllAnimals()

    /** Filtered list based on UI interactions (search, filters, sorting) */
    private val _animalsFiltered = MutableLiveData<List<Animal>>(emptyList())
    val animalsFiltered: LiveData<List<Animal>> = _animalsFiltered

    /** Selected animal for the details page */
    private val _selectedAnimal = MutableLiveData<Animal?>()
    val selectedAnimal: LiveData<Animal?> = _selectedAnimal

    /** List of favorite animals (local only, no Firebase sync) */
    private val _favorites = MutableLiveData<List<Animal>>(emptyList())
    val favorites: LiveData<List<Animal>> = _favorites

    /** Loading & error states (useful for UI feedback) */
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Initializes Firebase listener and loads latest data on ViewModel creation.
     */
    init {
        repository.startFirebaseListener()
        refreshFromFirebase()
    }

    /**
     * Manually refreshes local data by pulling latest values from Firebase.
     */
    fun refreshFromFirebase() = viewModelScope.launch {
        _isLoading.value = true
        try {
            repository.fetchAnimals()
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    // ------------------ Filtering, Sorting and Search -------------------

    /** Filters animals by species (Dog, Cat...) */
    fun filterBySpecies(species: String) = viewModelScope.launch {
        _animalsFiltered.value = repository.filterBySpecies(species)
    }

    /** Filters animals by size (Small, Medium, Large) */
    fun filterBySize(size: String) = viewModelScope.launch {
        _animalsFiltered.value = repository.filterBySize(size)
    }

    /** Sorts animals alphabetically */
    fun sortByName() = viewModelScope.launch {
        _animalsFiltered.value = repository.sortByName()
    }

    /** Sorts animals by age */
    fun sortByAge() = viewModelScope.launch {
        _animalsFiltered.value = repository.sortByAge()
    }

    /** Clears filters and resets list back to full list */
    fun clearFilters() {
        _animalsFiltered.value = emptyList()
    }

    /** Performs case-insensitive search by name */
    fun searchByName(text: String) = viewModelScope.launch {
        val baseList = animals.value ?: emptyList()
        _animalsFiltered.value = baseList.filter {
            it.name.contains(text, ignoreCase = true)
        }
    }

    // ------------------ Details Screen -------------------

    /** Loads selected animal by ID for details page */
    fun selectAnimal(id: Int) = viewModelScope.launch {
        _selectedAnimal.value = repository.getAnimalById(id)
    }

    // ------------------ Favorites -------------------

    /** Adds or removes animal from favorites list */
    fun toggleFavorite(animal: Animal) {
        val list = _favorites.value ?: emptyList()
        _favorites.value =
            if (list.any { it.id == animal.id })
                list.filterNot { it.id == animal.id }
            else
                list + animal
    }

    /** Clears any stored errors */
    fun clearError() { _error.value = null }
}
