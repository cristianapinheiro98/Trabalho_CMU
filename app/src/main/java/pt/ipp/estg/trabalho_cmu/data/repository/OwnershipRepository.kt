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

    fun getOwnershipsByUser(userFirebaseUid: String): LiveData<List<Ownership>> =
        ownershipDao.getOwnershipsByUser(userFirebaseUid)

    fun getOwnershipsByShelter(shelterFirebaseUid: String): LiveData<List<Ownership>> =
        ownershipDao.getOwnershipsByShelter(shelterFirebaseUid)

    suspend fun getOwnershipById(ownershipId: Int): Ownership? =
        ownershipDao.getOwnershipById(ownershipId)

    suspend fun createOwnership(ownership: Ownership): Result<Ownership> = withContext(Dispatchers.IO) {
        try {
            val existingRequest = ownershipDao.getExistingRequest(
                userFirebaseUid = ownership.userFirebaseUid,
                animalFirebaseUid = ownership.animalFirebaseUid
            )

            if (existingRequest != null) {
                return@withContext Result.failure(
                    Exception("You already have an ownership request for this animal!")
                )
            }

            // 1. Create in Room (offline-first)
            val roomId = ownershipDao.insertOwnership(ownership).toInt()
            val ownershipWithRoomId = ownership.copy(id = roomId)

            // 2. Try Firebase
            try {
                val data = hashMapOf(
                    "id" to roomId,
                    "userFirebaseUid" to ownershipWithRoomId.userFirebaseUid,
                    "animalFirebaseUid" to ownershipWithRoomId.animalFirebaseUid,
                    "shelterFirebaseUid" to ownershipWithRoomId.shelterFirebaseUid,
                    "ownerName" to ownershipWithRoomId.ownerName,
                    "status" to ownershipWithRoomId.status.name,
                    "createdAt" to ownershipWithRoomId.createdAt
                )

                val docRef = firestore.collection("ownerships").add(data).await()
                val firebaseUid = docRef.id

                // 3. Update Room with firebaseUid
                val finalOwnership = ownershipWithRoomId.copy(firebaseUid = firebaseUid)
                ownershipDao.insertOwnership(finalOwnership)

                Result.success(finalOwnership)
            } catch (e: Exception) {
                println("Firebase offline, ownership saved locally only")
                Result.success(ownershipWithRoomId)
            }
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

            // Buscar firebaseUid do ownership
            val ownership = ownershipDao.getOwnershipById(ownershipId)
            val firebaseUid = ownership?.firebaseUid

            if (firebaseUid != null) {
                firestore.collection("ownerships")
                    .document(firebaseUid)
                    .update("status", OwnershipStatus.APPROVED.name)
                    .await()
            }
        } catch (e: Exception) {
            println("Error approving ownership request: ${e.message}")
            throw e
        }
    }

    suspend fun rejectOwnershipRequest(ownershipId: Int) = withContext(Dispatchers.IO) {
        try {
            ownershipDao.updateOwnershipStatus(ownershipId, OwnershipStatus.REJECTED)

            val ownership = ownershipDao.getOwnershipById(ownershipId)
            val firebaseUid = ownership?.firebaseUid

            if (firebaseUid != null) {
                firestore.collection("ownerships")
                    .document(firebaseUid)
                    .update("status", OwnershipStatus.REJECTED.name)
                    .await()
            }
        } catch (e: Exception) {
            println("Error rejecting ownership request: ${e.message}")
            throw e
        }
    }

    suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus) = withContext(Dispatchers.IO) {
        try {
            ownershipDao.updateOwnershipStatus(id, status)

            val ownership = ownershipDao.getOwnershipById(id)
            val firebaseUid = ownership?.firebaseUid

            if (firebaseUid != null) {
                firestore.collection("ownerships")
                    .document(firebaseUid)
                    .update("status", status.name)
                    .await()
            }
        } catch (e: Exception) {
            println("Error updating ownership status: ${e.message}")
            throw e
        }
    }

    suspend fun deleteOwnership(ownership: Ownership) = withContext(Dispatchers.IO) {
        try {
            ownershipDao.deleteOwnership(ownership)

            val firebaseUid = ownership.firebaseUid
            if (firebaseUid != null) {
                firestore.collection("ownerships")
                    .document(firebaseUid)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            println("Error deleting ownership: ${e.message}")
            throw e
        }
    }

    suspend fun syncPendingOwnerships() = withContext(Dispatchers.IO) {
        try {
            val pending = ownershipDao.getOwnershipsWithoutFirebaseUid()
            pending.forEach { ownership ->
                try {
                    val data = hashMapOf(
                        "id" to ownership.id,
                        "userFirebaseUid" to ownership.userFirebaseUid,
                        "animalFirebaseUid" to ownership.animalFirebaseUid,
                        "shelterFirebaseUid" to ownership.shelterFirebaseUid,
                        "ownerName" to ownership.ownerName,
                        "status" to ownership.status.name,
                        "createdAt" to ownership.createdAt
                    )
                    val docRef = firestore.collection("ownerships").add(data).await()
                    ownershipDao.insertOwnership(ownership.copy(firebaseUid = docRef.id))
                } catch (e: Exception) {
                    println("Failed to sync ownership ${ownership.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error syncing pending ownerships: ${e.message}")
        }
    }

    private fun DocumentSnapshot.toOwnership(): Ownership? = try {
        Ownership(
            id = (getLong("id") ?: 0).toInt(),
            firebaseUid = id,
            userFirebaseUid = getString("userFirebaseUid") ?: "",
            animalFirebaseUid = getString("animalFirebaseUid") ?: "",
            shelterFirebaseUid = getString("shelterFirebaseUid") ?: "",
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