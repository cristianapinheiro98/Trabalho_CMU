package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

/**
 * Repository for managing Shelter data.
 * No Hilt injection - constructor receives DAO directly.
 */
class ShelterRepository(private val shelterDao: ShelterDao) {

    fun getAllShelters(): LiveData<List<Shelter>> = shelterDao.getAllShelters()

    suspend fun getShelterById(id: Int): Shelter? = shelterDao.getShelterById(id)

    suspend fun insertShelter(shelter: Shelter) = shelterDao.insertShelter(shelter)
}