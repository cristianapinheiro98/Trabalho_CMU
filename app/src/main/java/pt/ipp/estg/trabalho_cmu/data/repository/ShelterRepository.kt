package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import javax.inject.Inject

class ShelterRepository @Inject constructor(
    private val shelterDao: ShelterDao
) {
    fun getAllShelters() = shelterDao.getAllShelters()
    suspend fun getShelterById(id: Int) = shelterDao.getShelterById(id)
}
