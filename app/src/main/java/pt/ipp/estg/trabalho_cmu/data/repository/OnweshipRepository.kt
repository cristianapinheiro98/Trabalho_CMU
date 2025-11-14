package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

class OwnershipRepository(
    private val ownershipDao: OwnershipDao,
    private val firestore: FirebaseFirestore
) {

    fun getOwnershipsByUser(userId: Int): LiveData<List<Ownership>> =
        ownershipDao.getOwnershipsByUser(userId)

    fun getOwnershipsByShelter(shelterId: Int): LiveData<List<Ownership>> =
        ownershipDao.getOwnershipsByShelter(shelterId)

    suspend fun getOwnershipById(ownershipId: Int): Ownership? =
        ownershipDao.getOwnershipById(ownershipId)

    suspend fun createOwnership(ownership: Ownership): Result<Unit> = withContext(Dispatchers.IO) {
        try {

            val existingRequest = ownershipDao.getExistingRequest(
                userId = ownership.userId,
                animalId = ownership.animalId
            )

            if (existingRequest != null) {
                return@withContext Result.failure(
                    Exception("You already have an ownerhsip request for this animal!")
                )
            }
            val generatedId = ownershipDao.insertOwnership(ownership).toInt()

            val ownershipWithId = ownership.copy(id = generatedId)

            val data = hashMapOf(
                "id" to generatedId,
                "userId" to ownershipWithId.userId,
                "animalId" to ownershipWithId.animalId,
                "shelterId" to ownershipWithId.shelterId,
                "ownerName" to ownershipWithId.ownerName,
                "status" to ownershipWithId.status.name,
                "createdAt" to ownershipWithId.createdAt
            )
            firestore.collection("ownerships")
                .document(generatedId.toString())
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun fetchOwnerships() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("ownerships").get().await()
            val ownerships = snapshot.documents.mapNotNull { it.toOwnership() }

            if (ownerships.isNotEmpty()) {
                ownershipDao.insertAll(ownerships)
            }
        } catch (e: Exception) {
            println("Error getting ownerships from firebase: ${e.message}")
        }
    }

    suspend fun approveOwnershipRequest(ownershipId: Int) = withContext(Dispatchers.IO) {
        try {
            ownershipDao.updateOwnershipStatus(ownershipId, OwnershipStatus.APPROVED)

            firestore.collection("ownerships")
                .document(ownershipId.toString())
                .update("status", OwnershipStatus.APPROVED.name)
                .await()
        } catch (e: Exception) {
            println("Error approving ownership request: ${e.message}")
            throw e
        }
    }

    suspend fun rejectOwnershipRequest(ownershipId: Int) = withContext(Dispatchers.IO) {
        try {
            ownershipDao.updateOwnershipStatus(ownershipId, OwnershipStatus.REJECTED)

            firestore.collection("ownerships")
                .document(ownershipId.toString())
                .update("status", OwnershipStatus.REJECTED.name)
                .await()
        } catch (e: Exception) {
            println("Error rejecting ownership request: ${e.message}")
            throw e
        }
    }

    suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus) = withContext(Dispatchers.IO) {
        try {
            ownershipDao.updateOwnershipStatus(id, status)

            firestore.collection("ownerships")
                .document(id.toString())
                .update("status", status.name)
                .await()
        } catch (e: Exception) {
            println("Error updating ownership status: ${e.message}")
            throw e
        }
    }

    suspend fun deleteOwnership(ownership: Ownership) = withContext(Dispatchers.IO) {
        try {
            ownershipDao.deleteOwnership(ownership)

            firestore.collection("ownerships")
                .document(ownership.id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            println("Error deleting ownership ownership: ${e.message}")
            throw e
        }
    }

    private fun DocumentSnapshot.toOwnership(): Ownership? = try {
        Ownership(
            id = (getLong("id") ?: 0).toInt(),
            userId = (getLong("userId") ?: 0).toInt(),
            animalId = (getLong("animalId") ?: 0).toInt(),
            shelterId = (getLong("shelterId") ?: 0).toInt(),
            ownerName = getString("ownerName") ?: "",
            accountNumber = "",
            cvv = "",
            cardNumber = "",
            status = OwnershipStatus.valueOf(getString("status") ?: "PENDING"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        println("Error converting ownership: ${e.message}")
        null
    }
}