package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.mappers.toAnimal
import pt.ipp.estg.trabalho_cmu.data.local.mappers.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import pt.ipp.estg.trabalho_cmu.utils.StringHelper

/**
 * Repository responsible for managing animal-related data.
 *
 * Responsibilities:
 *  - CRUD operations for animals
 *  - Synchronizing Firebase to Room cache
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

    /** Returns all animals stored locally. */
    suspend fun getAllAnimalsList(): List<Animal> = animalDao.getAllAnimalsList()

    /** Retrieve an animal by ID. */
    suspend fun getAnimalById(animalId: String): Animal? = animalDao.getAnimalById(animalId)


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
                    val msg = StringHelper.getString(appContext, R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                val docRef = firestore.collection("animals")
                    .add(animal.toFirebaseMap())
                    .await()

                val savedAnimal = animal.copy(id = docRef.id)

                Result.success(savedAnimal)

            } catch (e: Exception) {
                val msg = StringHelper.getString(appContext, R.string.error_create_animal)
                Log.e(TAG, msg, e)
                Result.failure(Exception(msg))
            }
        }


    /**
     * Syncs all AVAILABLE animals from Firebase to Room.
     * Completely replaces the Room cache.
     */
    suspend fun syncAnimals() = withContext(Dispatchers.IO) {

        if (!NetworkUtils.isConnected()) return@withContext

        try {
            val snapshot = firestore.collection("animals")
                .whereEqualTo("status", AnimalStatus.AVAILABLE.name)
                .get().await()

            val animals = snapshot.documents.mapNotNull { it.toAnimal() }

            animalDao.insertAll(animals)

            Log.d(TAG, "SyncAnimals: ${animals.size} received")

        } catch (e: Exception) {
            val msg = StringHelper.getString(appContext, R.string.error_sync_animals)
            Log.e(TAG, msg, e)
        }
    }

    /**
     * Syncs animals owned by a specific user from Firebase to Room.
     *
     * Fetches approved ownerships for the user from Room
     * Extracts animal IDs from those ownerships
     * Fetches the corresponding animals from Firebase (chunked to respect Firestore 'in' limit)
     * Inserts/updates the animals in Room
     *
     * @param userId The user ID whose owned animals should be synced.
     */
    suspend fun syncUserOwnedAnimals(userId: String) = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext

        try {
            val ownershipSnapshot = firestore.collection("ownerships")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "APPROVED")
                .get().await()

            val animalIds = ownershipSnapshot.documents.mapNotNull {
                it.getString("animalId")
            }

            if (animalIds.isEmpty()) {
                Log.d(TAG, "SyncUserOwnedAnimals: No owned animals found for user $userId")
                return@withContext
            }

            val animals = mutableListOf<Animal>()

            animalIds.chunked(10).forEach { chunk ->
                val snapshot = firestore.collection("animals")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get().await()

                animals.addAll(snapshot.documents.mapNotNull { it.toAnimal() })
            }

            animalDao.insertAll(animals)

            Log.d(TAG, "SyncUserOwnedAnimals: ${animals.size} owned animals synced for user $userId")

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing owned animals for user $userId", e)
        }
    }


    /**
     * Retrieves multiple animals by their IDs from the local Room database.
     *
     * @param animalIds List of animal IDs.
     * @return List of animals matching the provided IDs.
     */
    suspend fun getAnimalsByIds(animalIds: List<String>): List<Animal> =
        animalDao.getAnimalsByIds(animalIds)

    /**
     * Syncs a specific animal from Firebase to Room.
     * Useful when you need to ensure a single animal exists locally.
     */
    suspend fun syncSpecificAnimal(animalId: String): Result<Animal> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) {
            return@withContext Result.failure(Exception("Offline"))
        }

        try {
            val snapshot = firestore.collection("animals")
                .document(animalId)
                .get()
                .await()

            val animal = snapshot.toAnimal()
            if (animal != null) {
                animalDao.insert(animal)
                Log.d(TAG, "SyncSpecificAnimal: Animal $animalId synced successfully")
                Result.success(animal)
            } else {
                Result.failure(Exception("Animal not found in Firebase"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing specific animal $animalId", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves animals owned by a specific user.
     * Fetches approved ownerships from Firebase and returns corresponding animals from Room.
     *
     * @param userId The user ID whose owned animals should be retrieved.
     * @return List of animals owned by the user.
     */
    suspend fun getOwnedAnimals(userId: String): List<Animal> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "DEBUG getOwnedAnimals: Starting for userId = $userId")

            // Fetch approved ownerships directly from Firebase
            val ownershipSnapshot = firestore.collection("ownerships")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "APPROVED")
                .get().await()

            Log.d(TAG, "DEBUG getOwnedAnimals: Found ${ownershipSnapshot.documents.size} ownership documents")

            val animalIds = ownershipSnapshot.documents.mapNotNull {
                val animalId = it.getString("animalId")
                Log.d(TAG, "DEBUG getOwnedAnimals: Ownership doc ${it.id} -> animalId = $animalId, status = ${it.getString("status")}")
                animalId
            }

            Log.d(TAG, "DEBUG getOwnedAnimals: Extracted ${animalIds.size} animalIds: $animalIds")

            if (animalIds.isEmpty()) {
                Log.d(TAG, "DEBUG getOwnedAnimals: No approved ownerships for user $userId")
                return@withContext emptyList()
            }

            // Get animals from Room
            val animals = animalDao.getAnimalsByIds(animalIds)
            Log.d(TAG, "DEBUG getOwnedAnimals: Room returned ${animals.size} animals")
            animals.forEach {
                Log.d(TAG, "DEBUG getOwnedAnimals: Animal id=${it.id}, name=${it.name}")
            }

            animals
        } catch (e: Exception) {
            Log.e(TAG, "DEBUG getOwnedAnimals: Exception", e)
            emptyList()
        }
    }
}
