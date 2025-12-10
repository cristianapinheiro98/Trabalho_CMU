package pt.ipp.estg.trabalho_cmu.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toShelter

/**
 * Repository responsible for working with shelter data coming from:
 *  - Local Room database (offline cache)
 *  - Firebase Firestore (remote source)
 *
 * Main responsibilities:
 *  - Retrieve shelter lists or individual shelters from Room
 *  - Update shelter information in Firestore
 *  - Synchronize all shelters from Firestore into Room
 *  - Provide LiveData streams for UI observation
 *
 * This repository ensures:
 *  - Firestore operations only run when online
 *  - Local fallback when offline
 */
class ShelterRepository(
    private val shelterDao: ShelterDao
) {
    private val firestore = FirebaseProvider.firestore
    private val TAG = "ShelterRepository"

    /** Returns a LiveData stream of all shelters stored locally. */
    fun getAllShelters(): LiveData<List<Shelter>> = shelterDao.getAllShelters()

    /** Retrieves all shelters from Room as a list. */
    suspend fun getAllSheltersList(): List<Shelter> = shelterDao.getAllSheltersList()

    /** Retrieves a single shelter by its ID from Room. */
    suspend fun getShelterById(id: String) = shelterDao.getShelterById(id)

    /**
     * Synchronizes all shelters from Firestore and replaces the local cache.
     *
     * If offline, the function stops silently.
     */
    suspend fun syncShelters() = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext

        try {
            val snapshot = firestore.collection("shelters").get().await()
            val shelters = snapshot.documents.mapNotNull { it.toShelter() }

            shelterDao.insertAll(shelters)
            Log.d(TAG, "SyncShelters: ${shelters.size} recebidos")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
