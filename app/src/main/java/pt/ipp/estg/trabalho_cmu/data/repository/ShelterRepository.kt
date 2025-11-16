package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider


class ShelterRepository(
    private val shelterDao: ShelterDao
) {
    private val firestore = FirebaseProvider.firestore
    // Online first then offline (Room)
    fun getAllShelters(): LiveData<List<Shelter>> {
        refreshSheltersFromFirebase()
        return shelterDao.getAllShelters()
    }

    private fun refreshSheltersFromFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("shelters").get().await()
                val shelters = snapshot.documents.mapNotNull { doc ->
                    Shelter(
                        firebaseUid = doc.id,
                        name = doc.getString("name") ?: "",
                        address = doc.getString("address") ?: "",
                        phone = doc.getString("contact") ?: "",
                        email = doc.getString("email") ?: "",
                        password = ""
                    )
                }
                shelterDao.insertShelters(shelters)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun getAllSheltersList(): List<Shelter> = shelterDao.getAllSheltersList()
    suspend fun getShelterById(id: Int): Shelter? = shelterDao.getShelterById(id)

    suspend fun getShelterByFirebaseUid(uid: String) = shelterDao.getShelterByFirebaseUid(uid)
    suspend fun insertShelter(shelter: Shelter) = shelterDao.insertShelter(shelter)
    suspend fun updateShelter(shelter: Shelter) = shelterDao.updateShelter(shelter)
    suspend fun deleteShelter(shelter: Shelter) = shelterDao.deleteShelter(shelter)

    suspend fun deleteShelterById(id: Int) = shelterDao.deleteShelterById(id)
    suspend fun searchSheltersByName(name: String): List<Shelter> =
        shelterDao.searchSheltersByName("%$name%")
}