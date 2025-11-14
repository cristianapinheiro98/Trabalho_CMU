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

class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnimalRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        AnimalRepository(db.animalDao(), FirebaseFirestore.getInstance())
    }

    val animals: LiveData<List<Animal>> = repository.getAllAnimals()

    private val _animalsFiltered = MutableLiveData<List<Animal>>(emptyList())
    val animalsFiltered: LiveData<List<Animal>> = _animalsFiltered

    private val _selectedAnimal = MutableLiveData<Animal?>()
    val selectedAnimal: LiveData<Animal?> = _selectedAnimal

    private val _favorites = MutableLiveData<List<Animal>>(emptyList())
    val favorites: LiveData<List<Animal>> = _favorites

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        repository.startFirebaseListener()
        refreshFromFirebase()
    }

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

    // ------------------ Filters, Sorting and Search -------------------

    fun filterBySpecies(species: String) = viewModelScope.launch {
        _animalsFiltered.value = repository.filterBySpecies(species)
    }

    fun filterBySize(size: String) = viewModelScope.launch {
        _animalsFiltered.value = repository.filterBySize(size)
    }

    fun sortByName() = viewModelScope.launch {
        _animalsFiltered.value = repository.sortByName()
    }

    fun sortByAge() = viewModelScope.launch {
        _animalsFiltered.value = repository.sortByAge()
    }

    fun sortByDate() = viewModelScope.launch {
        _animalsFiltered.value = repository.sortByDate()
    }

    fun clearFilters() {
        _animalsFiltered.value = emptyList()
    }

    fun searchByName(text: String) = viewModelScope.launch {
        val baseList = animals.value ?: emptyList()
        _animalsFiltered.value = baseList.filter {
            it.name.contains(text, ignoreCase = true)
        }
    }


    // ------------------ Page Details -------------------

    fun selectAnimal(id: Int) = viewModelScope.launch {
        _selectedAnimal.value = repository.getAnimalById(id)
    }

    // ------------------ Favorites  -------------------

    fun toggleFavorite(animal: Animal) {
        val list = _favorites.value ?: emptyList()
        _favorites.value =
            if (list.any { it.id == animal.id })
                list.filterNot { it.id == animal.id }
            else
                list + animal
    }

    fun clearError() { _error.value = null }
}
