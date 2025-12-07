package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toOwnership
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap

/**
 * Repository responsible for managing ownership (adoption) requests.
 *
 * - Creating ownership requests (user-side)
 * - Approving / rejecting requests (shelter-side)
 * - Syncing ownership data from Firestore to Room
 * - Providing offline cache through Room
 *
 * Depends on Context to produce localized error strings.
 */
class OwnershipRepository(
    private val appContext: Context,
    private val ownershipDao: OwnershipDao
) {

    private val firestore: FirebaseFirestore = FirebaseProvider.firestore

    /** Returns LiveData of pending ownerships requested by a user. */
    fun getPendingOwnershipsByUser(userId: String) =
        ownershipDao.getPendingOwnershipsByUser(userId)

    /** Returns LiveData of pending ownerships for a shelter. */
    fun getPendingOwnershipsByShelter(shelterId: String) =
        ownershipDao.getPendingOwnershipsByShelter(shelterId)

    /** Retrieve ownership request by ID. */
    suspend fun getOwnershipById(id: String) = ownershipDao.getOwnershipById(id)

    /**
     * Creates an adoption request.
     *
     * Validates:
     * - Internet connection
     * - Duplicate request for same user + animal
     */
    suspend fun createOwnership(ownership: Ownership): Result<Ownership> =
        withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isConnected()) {
                    val msg = appContext.getString(R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                // Prevent duplicate requests
                if (ownershipDao.getExistingRequest(ownership.userId, ownership.animalId) != null) {
                    val msg = appContext.getString(R.string.error_duplicate_request)
                    return@withContext Result.failure(Exception(msg))
                }

                val docRef = firestore.collection("ownerships")
                    .add(ownership.toFirebaseMap())
                    .await()

                val savedOwnership = ownership.copy(id = docRef.id)
                Result.success(savedOwnership)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Shelter approves an adoption request.
     *
     * Updates:
     * - ownership.status = APPROVED
     * - animal.status = HASOWNED
     */
    suspend fun approveOwnershipRequest(
        ownershipId: String,
        animalId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {

            if (!NetworkUtils.isConnected()) {
                val msg = appContext.getString(R.string.error_offline)
                return@withContext Result.failure(Exception(msg))
            }

            firestore.runTransaction { transaction ->
                val ownRef = firestore.collection("ownerships").document(ownershipId)
                val animRef = firestore.collection("animals").document(animalId)

                transaction.update(ownRef, "status", OwnershipStatus.APPROVED.name)
                transaction.update(animRef, "status", "HASOWNED")
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            val msg = appContext.getString(R.string.error_update_status)
            Result.failure(Exception(msg))
        }
    }

    /**
     * Rejects an adoption request.
     */
    suspend fun rejectOwnershipRequest(ownershipId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {

                if (!NetworkUtils.isConnected()) {
                    val msg = appContext.getString(R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                firestore.collection("ownerships")
                    .document(ownershipId)
                    .update("status", OwnershipStatus.REJECTED.name)
                    .await()

                Result.success(Unit)

            } catch (e: Exception) {
                val msg = appContext.getString(R.string.error_update_status)
                Result.failure(Exception(msg))
            }
        }

    /**
     * Syncs pending ownership requests for a given shelter.
     *
     * Replaces local Room cache.
     */
    suspend fun syncPendingOwnerships(shelterId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!NetworkUtils.isConnected()) {
                val msg = appContext.getString(R.string.error_offline)
                return@withContext Result.failure(Exception(msg))
            }

            try {
                val snapshot = firestore.collection("ownerships")
                    .whereEqualTo("shelterId", shelterId)
                    .whereEqualTo("status", OwnershipStatus.PENDING.name)
                    .get().await()

                val list = snapshot.documents.mapNotNull { it.toOwnership() }
                ownershipDao.refreshCache(list)

                Result.success(Unit)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Room: returns approved ownerships for a user. */
    suspend fun getApprovedOwnershipsByUser(userId: String) =
        ownershipDao.getApprovedOwnershipsByUser(userId)

    /**
     * Syncs approved requests for a user.
     * (No error string required because offline simply means "do nothing")
     */
    suspend fun syncUserApprovedOwnerships(userId: String) =
        withContext(Dispatchers.IO) {
            if (!NetworkUtils.isConnected()) return@withContext

            try {
                val snapshot = firestore.collection("ownerships")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", OwnershipStatus.APPROVED.name)
                    .get().await()

                val list = snapshot.documents.mapNotNull { it.toOwnership() }
                ownershipDao.insertAll(list)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}
