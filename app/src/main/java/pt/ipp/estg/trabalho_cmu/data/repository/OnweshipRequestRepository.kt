package pt.ipp.estg.trabalho_cmu.data.repository


import androidx.lifecycle.LiveData
import pt.ipp.estg.trabalho_cmu.data.local.dao.OnwershipRequestDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.OwnershipRequest
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus

/**
 * Repository that handles data operations for Ownerships.
 * Provides a clean API for ViewModels and hides data source details.
 */
open class OwnershipRequestRepository(private val ownershipDao: OnwershipRequestDao) {

    open fun getOwnershipsByUser(userId: String): LiveData<List<OwnershipRequest>> =
        ownershipDao.getOwnershipsByUser(userId)

    open suspend fun addOwnership(ownership: OwnershipRequest) {
        ownershipDao.insertOwnership(ownership)
    }

    open suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        ownershipDao.updateOwnershipStatus(id, status)
    }

    open suspend fun deleteOwnership(ownership: OwnershipRequest) {
        ownershipDao.deleteOwnership(ownership)
    }
}
