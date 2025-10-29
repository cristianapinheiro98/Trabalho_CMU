package pt.ipp.estg.trabalho_cmu.data.repositories

import androidx.lifecycle.LiveData
import pt.ipp.estg.trabalho_cmu.data.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.entities.OwnershipStatus
import pt.ipp.estg.trabalho_cmu.data.local.entities.OwnershipRequest
import pt.ipp.estg.trabalho_cmu.data.local.entities.OwnershipStatus

/**
 * Repository that handles data operations for Ownerships.
 * Provides a clean API for ViewModels and hides data source details.
 */
class OwnershipRepository(private val ownershipDao: OwnershipDao) {

    fun getOwnershipsByUser(userId: String): LiveData<List<OwnershipRequest>> =
        ownershipDao.getOwnershipsByUser(userId)

    suspend fun addOwnership(ownership: OwnershipRequest) {
        ownershipDao.insertOwnership(ownership)
    }

    suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        ownershipDao.updateOwnershipStatus(id, status)
    }

    suspend fun deleteOwnership(ownership: OwnershipRequest) {
        ownershipDao.deleteOwnership(ownership)
    }
}
