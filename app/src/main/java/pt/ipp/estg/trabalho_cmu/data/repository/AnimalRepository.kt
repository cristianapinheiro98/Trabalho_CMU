package pt.ipp.estg.trabalho_cmu.data.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.mappers.toAnimal
import pt.ipp.estg.trabalho_cmu.data.local.mappers.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils

class AnimalRepository(
    private val animalDao: AnimalDao,
    private val application: Application
) {
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
    private val TAG = "AnimalRepository"

    // --- LEITURA (Cache Room) ---
    // A UI observa isto. O sync atualiza o Room, e o Room notifica a UI.
    fun getAllAnimals(): LiveData<List<Animal>> = animalDao.getAllAnimals()
    suspend fun getAllAnimalsList(): List<Animal> = animalDao.getAllAnimalsList()

    suspend fun getAnimalById(animalId: String): Animal? = animalDao.getAnimalById(animalId)

    suspend fun getAnimalsFromRoom(): List<Animal> {
        return animalDao.getAllAnimalsList()
    }

    // --- FILTROS LOCAIS ---
    suspend fun filterBySpecies(species: String) = animalDao.filterBySpeciesLocal(species)
    suspend fun filterBySize(size: String) = animalDao.filterBySizeLocal(size)
    suspend fun sortByName() = animalDao.sortByNameLocal()
    suspend fun sortByAge() = animalDao.sortByAgeLocal()
    suspend fun sortByDate() = animalDao.sortByDateLocal()
    suspend fun searchAnimals(query: String) = animalDao.searchAnimalsLocal(query)

    // --- ESCRITA (Apenas Firebase) ---
    suspend fun createAnimal(animal: Animal): Result<Animal> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) {
                return@withContext Result.failure(Exception("Offline. Impossível criar animal."))
            }

            // 1. Enviar para Firebase
            val docRef = firestore.collection("animals").add(animal.toFirebaseMap()).await()
            val savedAnimal = animal.copy(id = docRef.id)

            // NOTA: Não inserimos no Room manualmente.
            // O ViewModel deve chamar syncAnimals() após o sucesso para atualizar a lista.

            Result.success(savedAnimal)
        } catch (e: Exception) {
            Log.e(TAG, "Erro createAnimal", e)
            Result.failure(e)
        }
    }

    suspend fun changeAnimalStatusToOwned(animalId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline."))

            firestore.collection("animals").document(animalId)
                .update("status", AnimalStatus.HASOWNED.name).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SINCRONIZAÇÃO ---
    suspend fun syncAnimals() = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext

        try {
            val snapshot = firestore.collection("animals")
                .whereEqualTo("status", AnimalStatus.AVAILABLE.name)
                .get().await()

            // Mapear e Atualizar Room (Esmaga a cache antiga)
            val animals = snapshot.documents.mapNotNull { it.toAnimal() }
            animalDao.refreshCache(animals)

            Log.d(TAG, "SyncAnimals: ${animals.size} recebidos")
        } catch (e: Exception) {
            Log.e(TAG, "Erro SyncAnimals", e)
        }
    }
}