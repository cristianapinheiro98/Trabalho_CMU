package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import retrofit2.HttpException
import java.io.IOException

open class AnimalViewModel(
    private val repository: AnimalRepository
) : ViewModel() {

    private val _animals = MutableLiveData<List<Animal>>(emptyList())
    val animals: LiveData<List<Animal>> = _animals

    private val _favorites = MutableLiveData<List<Animal>>(emptyList())
    open val favorites: LiveData<List<Animal>> = _favorites

    private val _selectedAnimal = MutableLiveData<Animal?>()
    val selectedAnimal: LiveData<Animal?> = _selectedAnimal

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        loadAnimals()
    }

    fun loadAnimals() = viewModelScope.launch {
        _isLoading.value = true
        try {
            _animals.value = repository.fetchAnimals()
        } finally {
            _isLoading.value = false
        }
    }

    fun applyFilters(
        species: String? = null,
        breed: String? = null,
        size: String? = null,
        color: String? = null,
        gender: String? = null
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            _animals.value = repository.filterAnimals(species, breed, size, color, gender)
        } finally {
            _isLoading.value = false
        }
    }

    fun applySort(
        sortBy: String,
        order: String
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            _animals.value = repository.sortAnimals(sortBy, order)
        } finally {
            _isLoading.value = false
        }
    }

    open fun toggleFavorite(animal: Animal) {
        val current = _favorites.value ?: emptyList()
        _favorites.value = if (current.any { it.id == animal.id }) {
            current.filterNot { it.id == animal.id }
        } else {
            current + animal
        }
    }

    fun selecionarAnimal(id: Int) {
        _selectedAnimal.value = _animals.value?.find { it.id == id }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}
