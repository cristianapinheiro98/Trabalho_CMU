package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus

/**
 * Repository responsible for managing ownership (adoption) requests
 * from the shelter's perspective.
 *
 * Allows listing all requests, approving, rejecting, and deleting them.
 */
class ShelterOwnershipRequestRepository(
    private val ownershipDao: OwnershipDao
) {

    /**
     * Retrieves all ownership requests.
     * You can modify the DAO to return only pending ones if desired.
     */
    fun getAllOwnershipRequestsByShelter(shelterId : Int): LiveData<List<Ownership>> =
        ownershipDao.getOwnershipsByShelter(shelterId)

    /**
     * Approves an ownership request and updates its status to APPROVED.
     */
    suspend fun approveOwnershipRequest(ownershipId: Int) = withContext(Dispatchers.IO) {
        ownershipDao.updateOwnershipStatus(ownershipId, OwnershipStatus.APPROVED)
    }

    /**
     * Rejects an ownership request and updates its status to REJECTED.
     */
    suspend fun rejectOwnershipRequest(ownershipId: Int) = withContext(Dispatchers.IO) {
        ownershipDao.updateOwnershipStatus(ownershipId, OwnershipStatus.REJECTED)
    }

    /**
     * Deletes an ownership request from the database.
     * (Useful if a request is canceled or invalidated.)
     */
    suspend fun deleteOwnershipRequest(ownership: Ownership) = withContext(Dispatchers.IO) {
        ownershipDao.deleteOwnership(ownership)
    }
}
