package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import retrofit2.HttpException
import java.io.IOException

open class AnimalViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao = AppDatabase.getDatabase(application).animalDao()
    private val repository = AnimalRepository(animalDao)

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

    /**
     * 🔹 Carrega animais do Room e tenta atualizar com a API
     */
    fun loadAnimals(sortBy: String? = null, order: String? = null) = viewModelScope.launch {
        _isLoading.value = true
        try {
            // 1️⃣ Lê dados locais do Room
            val localAnimals = withContext(Dispatchers.IO) {
                animalDao.getAllAnimals().value ?: emptyList()
            }
            _animals.value = localAnimals

            // 2️⃣ Busca os dados da API
            val remoteAnimals = repository.refreshAnimals(sortBy, order)

            if (remoteAnimals.isNotEmpty()) {
                _animals.value = remoteAnimals // atualiza UI com os novos
            } else if (localAnimals.isEmpty()) {
                _error.value = "Não foi possível carregar os dados nem localmente nem da API."
            }

        } catch (e: IOException) {
            _error.value = "Erro de rede: ${e.message}"
        } catch (e: HttpException) {
            _error.value = "Erro do servidor: ${e.code()}"
        } catch (e: Exception) {
            _error.value = "Erro inesperado: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /** ❤️ Alternar favoritos */
    open fun toggleFavorite(animal: Animal) {
        val current = _favorites.value ?: emptyList()
        _favorites.value = if (current.any { it.id == animal.id }) {
            current.filterNot { it.id == animal.id }
        } else {
            current + animal
        }
    }

    /** 🔍 Selecionar animal */
    fun selecionarAnimal(id: Int) {
        _selectedAnimal.value = _animals.value?.find { it.id == id }
    }

    fun clearMessage() { _message.value = null }
    fun clearError() { _error.value = null }
}
