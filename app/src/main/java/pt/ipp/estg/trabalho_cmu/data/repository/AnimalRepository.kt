package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.mappers.toAnimal
import pt.ipp.estg.trabalho_cmu.data.local.mappers.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils

/**
 * Repository responsible for managing animal-related data.
 *
 * Responsibilities:
 *  - CRUD operations for animals
 *  - Synchronizing Firebase → Room cache
 *  - Filtering, sorting, and searching using Room queries
 *
 * This repository depends on Context to fetch localized error strings.
 */
class AnimalRepository(
    private val appContext: Context,
    private val animalDao: AnimalDao
) {

    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
    private val TAG = "AnimalRepository"

    /** Live list of all available animals. */
    fun getAllAnimals(): LiveData<List<Animal>> = animalDao.getAllAnimals()

    /** Returns all animals stored locally. */
    suspend fun getAllAnimalsList(): List<Animal> = animalDao.getAllAnimalsList()

    /** Retrieve an animal by ID. */
    suspend fun getAnimalById(animalId: String): Animal? = animalDao.getAnimalById(animalId)

    /** Returns all animals stored in Room. */
    suspend fun getAnimalsFromRoom(): List<Animal> = animalDao.getAllAnimalsList()

    /** Filtering and sorting (Room-based). */
    suspend fun filterBySpecies(species: String) = animalDao.filterBySpeciesLocal(species)
    suspend fun filterBySize(size: String) = animalDao.filterBySizeLocal(size)
    suspend fun sortByNameAsc() = animalDao.sortByNameAscLocal()
    suspend fun sortByNameDesc() = animalDao.sortByNameDescLocal()
    suspend fun sortByAgeAsc() = animalDao.sortByAgeAscLocal()
    suspend fun sortByAgeDesc() = animalDao.sortByAgeDescLocal()
    suspend fun searchAnimals(query: String) = animalDao.searchAnimalsLocal(query)

    /**
     * Creates a new animal in Firestore.
     *
     * Returns:
     *  - Result.success(Animal)
     *  - Result.failure with a localized error message
     */
    suspend fun createAnimal(animal: Animal): Result<Animal> =
        withContext(Dispatchers.IO) {
            try {

                if (!NetworkUtils.isConnected()) {
                    val msg = appContext.getString(R.string.error_offline_create_animal)
                    return@withContext Result.failure(Exception(msg))
                }

                val docRef = firestore.collection("animals")
                    .add(animal.toFirebaseMap())
                    .await()

                val savedAnimal = animal.copy(id = docRef.id)

                Result.success(savedAnimal)

            } catch (e: Exception) {
                val msg = appContext.getString(R.string.error_create_animal)
                Log.e(TAG, msg, e)
                Result.failure(Exception(msg))
            }
        }

    /**
     * Updates an animal's status to HASOWNED after an adoption is approved.
     */
    suspend fun changeAnimalStatusToOwned(animalId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isConnected()) {
                    val msg = appContext.getString(R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                firestore.collection("animals")
                    .document(animalId)
                    .update("status", AnimalStatus.HASOWNED.name)
                    .await()

                Result.success(Unit)

            } catch (e: Exception) {
                val msg = appContext.getString(R.string.error_update_status)
                Result.failure(Exception(msg))
            }
        }

    /**
     * Syncs all AVAILABLE animals from Firebase → Room.
     * Completely replaces the Room cache.
     */
    suspend fun syncAnimals() = withContext(Dispatchers.IO) {

        if (!NetworkUtils.isConnected()) return@withContext

        try {
            val snapshot = firestore.collection("animals")
                .whereEqualTo("status", AnimalStatus.AVAILABLE.name)
                .get().await()

            val animals = snapshot.documents.mapNotNull { it.toAnimal() }

            animalDao.refreshCache(animals)

            Log.d(TAG, "SyncAnimals: ${animals.size} received")

        } catch (e: Exception) {
            val msg = appContext.getString(R.string.error_sync_animals)
            Log.e(TAG, msg, e)
        }
    }
}
