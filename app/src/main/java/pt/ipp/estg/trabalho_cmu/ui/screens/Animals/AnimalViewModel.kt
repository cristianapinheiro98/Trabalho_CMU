package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import androidx.lifecycle.*
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository

open class AnimalViewModel(
    private val repository: AnimalRepository? = null
) : ViewModel() {

    private val _animals = MutableLiveData<List<Animal>>(emptyList())
    open val animals: LiveData<List<Animal>> = _animals

    private val _favorites = MutableLiveData<List<Animal>>(emptyList())
    open val favorites: LiveData<List<Animal>> = _favorites

    private val _selectedAnimal = MutableLiveData<Animal?>()
    open val selectedAnimal: LiveData<Animal?> = _selectedAnimal

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        repository?.let { loadAnimals() }
    }

    fun loadAnimals() = viewModelScope.launch {
        loadDataSafely { repository?.fetchAnimals() ?: emptyList() }
    }
    private fun applyFilter(filterAction: suspend () -> List<Animal>) = viewModelScope.launch {
        loadDataSafely(filterAction)
    }

    fun filterBySpecies(species: String) = applyFilter { repository?.filterBySpecies(species) ?: emptyList() }
    fun filterBySize(size: String) = applyFilter { repository?.filterBySize(size) ?: emptyList() }
    fun filterByGender(gender: String) = applyFilter { repository?.filterByGender(gender) ?: emptyList() }
    fun sortByName() = viewModelScope.launch {
        _animals.value = repository?.sortByName("asc")
    }
    fun sortByAge() = viewModelScope.launch {
        _animals.value = repository?.sortByAge("desc")
    }



    open fun toggleFavorite(animal: Animal) {
        val current = _favorites.value ?: emptyList()
        _favorites.value = if (current.any { it.id == animal.id }) {
            current.filterNot { it.id == animal.id }
        } else {
            current + animal
        }
    }

    open fun selectAnimal(id: Int) {
        _selectedAnimal.value = _animals.value?.find { it.id == id }
    }

    private suspend fun loadDataSafely(block: suspend () -> List<Animal>) {
        _isLoading.value = true
        try {
            _animals.value = block()
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }

    fun clearSelectedAnimal() { _selectedAnimal.value = null }
}

