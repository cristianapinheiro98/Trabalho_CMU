package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val _animals = MutableLiveData<List<Animal>>(emptyList())
    val animals: LiveData<List<Animal>> = _animals

    private val _favorites = MutableLiveData<List<Animal>>(emptyList())
    val favorites: LiveData<List<Animal>> = _favorites

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

    /** 🔹 Simula o carregamento dos animais **/
    fun loadAnimals() = viewModelScope.launch {
        _isLoading.value = true
        delay(400)
        _animals.value = listOf(
            Animal(1, "Leia", "Siamês", "Gato", "Pequeno", "5", R.drawable.gato1, 1),
            Animal(2, "Noa", "Europeu", "Gato", "Médio", "2", R.drawable.gato2, 1),
            Animal(3, "Tito", "Maine Coon", "Gato", "Grande", "13", R.drawable.gato3, 1),
            Animal(4, "Pintas", "Europeu", "Gato", "Pequeno", "6", R.drawable.gato4, 1),
            Animal(5, "Branquinho", "Persa", "Gato", "Pequeno", "8", R.drawable.gato5, 1),
            Animal(6, "Riscas", "Europeu", "Gato", "Pequeno", "8", R.drawable.gato6, 1)
        )
        _isLoading.value = false
    }

    /** 🔹 Alternar favoritos **/
    fun toggleFavorite(animal: Animal) {
        val current = _favorites.value ?: emptyList()
        _favorites.value = if (current.any { it.id == animal.id }) {
            current.filterNot { it.id == animal.id }
        } else {
            current + animal
        }
    }

    /** 🔹 Selecionar animal para detalhe **/
    fun selecionarAnimal(id: Int) {
        _selectedAnimal.value = _animals.value?.find { it.id == id }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}
