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

class ShelterRepository(
    private val shelterDao: ShelterDao
) {
    private val firestore = FirebaseProvider.firestore
    private val TAG = "ShelterRepository"

    fun getAllShelters(): LiveData<List<Shelter>> = shelterDao.getAllShelters()
    suspend fun getAllSheltersList(): List<Shelter> = shelterDao.getAllSheltersList()
    suspend fun getShelterById(id: String) = shelterDao.getShelterById(id)

    // --- UPDATE (Firebase Only) ---
    suspend fun updateShelter(shelter: Shelter) = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) throw Exception("Offline")

        val data = mapOf(
            "name" to shelter.name,
            "address" to shelter.address,
            "contact" to shelter.phone
        )
        firestore.collection("shelters").document(shelter.id).update(data).await()
        // Não atualiza Room (espera sync)
    }

    // --- SYNC (Firebase -> Room) ---
    suspend fun syncShelters() = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext

        try {
            val snapshot = firestore.collection("shelters").get().await()
            val shelters = snapshot.documents.mapNotNull { it.toShelter() }
            shelterDao.refreshCache(shelters)
            Log.d(TAG, "SyncShelters: ${shelters.size} recebidos")
        } catch (e: Exception) { e.printStackTrace() }
    }
}