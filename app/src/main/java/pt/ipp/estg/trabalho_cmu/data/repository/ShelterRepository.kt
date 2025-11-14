package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter


class ShelterRepository(private val shelterDao: ShelterDao) {

    fun getAllShelters(): LiveData<List<Shelter>> = shelterDao.getAllShelters()
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