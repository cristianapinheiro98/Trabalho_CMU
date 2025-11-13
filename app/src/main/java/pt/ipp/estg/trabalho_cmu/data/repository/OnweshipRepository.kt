package pt.ipp.estg.trabalho_cmu.data.repository


import androidx.lifecycle.LiveData
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

/**
 * Repository that handles data operations for Ownerships.
 * Provides a clean API for ViewModels and hides data source details.
 */
open class OwnershipRepository(private val ownershipDao: OwnershipDao) {

    open fun getOwnershipsByUser(userId: Int): LiveData<List<Ownership>> =
        ownershipDao.getOwnershipsByUser(userId)

    suspend fun getOwnershipById(ownershipId: Int): Ownership? {
        return ownershipDao.getOwnershipById(ownershipId)
    }

    open suspend fun addOwnership(ownership: Ownership) {
        ownershipDao.insertOwnership(ownership)
    }

    open suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        ownershipDao.updateOwnershipStatus(id, status)
    }

    open suspend fun deleteOwnership(ownership: Ownership) {
        ownershipDao.deleteOwnership(ownership)
    }

}
