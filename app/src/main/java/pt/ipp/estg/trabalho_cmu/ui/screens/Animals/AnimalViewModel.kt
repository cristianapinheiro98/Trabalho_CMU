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

    private fun loadAnimals() {
        viewModelScope.launch {
            try {
                // 1) Atualiza Room com Firebase
                animalRepository.syncAnimals()

                // 2) Lê da Room depois do sync
                _animals.value = animalRepository.getAnimalsFromRoom()

            } catch (e: Exception) {
                // offline → fallback
                _animals.value = animalRepository.getAllAnimalsList()
            }
        }
}

    // ========== CREATE ANIMAL ==========
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
                _uiState.value = AnimalUiState.Error(exception.message ?: R.string.error_loading_animal.toString())
            }
    }

    // ========== FILTERS & SEARCH (Cache Room) ==========
    fun filterBySpecies(species: String) = viewModelScope.launch {
        Log.d(TAG, "filterBySpecies: $species")
        try {
            val result = animalRepository.filterBySpecies(species)
            _filteredAnimals.value = result
            Log.d(TAG, "Filtrados por espécie: ${result.size} animais")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao filtrar por espécie", e)
            _filteredAnimals.value = emptyList()
        }
    }

    fun filterBySize(size: String) = viewModelScope.launch {
        Log.d(TAG, "filterBySize: $size")
        try {
            val result = animalRepository.filterBySize(size)
            _filteredAnimals.value = result
            Log.d(TAG, "Filtrados por tamanho: ${result.size} animais")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao filtrar por tamanho", e)
            _filteredAnimals.value = emptyList()
        }
    }

    fun sortByName() = viewModelScope.launch {
        Log.d(TAG, "sortByName")
        try {
            val result = animalRepository.sortByName()
            _filteredAnimals.value = result
            Log.d(TAG, "Ordenados por nome: ${result.size} animais")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao ordenar por nome", e)
            _filteredAnimals.value = emptyList()
        }
    }

    fun sortByAge() = viewModelScope.launch {
        Log.d(TAG, "sortByAge")
        try {
            val result = animalRepository.sortByAge()
            _filteredAnimals.value = result
            Log.d(TAG, "Ordenados por idade: ${result.size} animais")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao ordenar por idade", e)
            _filteredAnimals.value = emptyList()
        }
    }

    fun sortByDate() = viewModelScope.launch {
        Log.d(TAG, "sortByDate")
        try {
            val result = animalRepository.sortByDate()
            _filteredAnimals.value = result
            Log.d(TAG, "Ordenados por data: ${result.size} animais")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao ordenar por data", e)
            _filteredAnimals.value = emptyList()
        }
    }

    fun searchAnimals(query: String) = viewModelScope.launch {
        Log.d(TAG, "searchAnimals: '$query'")
        try {
            val result = animalRepository.searchAnimals(query)
            _filteredAnimals.value = result
            Log.d(TAG, "Pesquisa retornou: ${result.size} animais")
        } catch (e: Exception) {
            Log.e(TAG, "Erro na pesquisa", e)
            _filteredAnimals.value = emptyList()
        }
    }

    fun clearFilters() {
        Log.d(TAG, "clearFilters")
        _filteredAnimals.value = emptyList()
    }

    // ========== SELECT ANIMAL ==========
    fun selectAnimal(animalId: String) = viewModelScope.launch {
        Log.d(TAG, "selectAnimal by id: $animalId")
        try {
            val animal = animalRepository.getAnimalById(animalId)
            _selectedAnimal.value = animal
            Log.d(TAG, "Animal selecionado: ${animal?.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao selecionar animal", e)
            _selectedAnimal.value = null
        }
    }

    fun refreshAnimals() {
        viewModelScope.launch {
            animalRepository.syncAnimals()
        }
    }

    fun selectAnimal(animal: Animal) {
        Log.d(TAG, "selectAnimal by object: ${animal.id}")
        _selectedAnimal.value = animal
    }

    fun clearSelectedAnimal() {
        Log.d(TAG, "clearSelectedAnimal")
        _selectedAnimal.value = null
    }
}