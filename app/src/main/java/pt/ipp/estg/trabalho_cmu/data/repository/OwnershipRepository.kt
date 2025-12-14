package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import android.util.Log
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
import pt.ipp.estg.trabalho_cmu.utils.StringHelper

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
                    val msg = StringHelper.getString(appContext, R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                val duplicateQuery = firestore.collection("ownerships")
                    .whereEqualTo("userId", ownership.userId)
                    .whereEqualTo("animalId", ownership.animalId)
                    .whereIn("status", listOf(OwnershipStatus.PENDING.name, OwnershipStatus.APPROVED.name))
                    .get()
                    .await()

                if (!duplicateQuery.isEmpty) {
                    val msg = StringHelper.getString(appContext, R.string.error_duplicate_request)
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
     * - Other pending requests for the same animal = REJECTED
     */
    suspend fun approveOwnershipRequest(
        ownershipId: String,
        animalId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {

            if (!NetworkUtils.isConnected()) {
                val msg = StringHelper.getString(appContext, R.string.error_offline)
                return@withContext Result.failure(Exception(msg))
            }

            // Fetch other pending ownerships for the same animal BEFORE the transaction
            val otherPendingSnapshot = firestore.collection("ownerships")
                .whereEqualTo("animalId", animalId)
                .whereEqualTo("status", OwnershipStatus.PENDING.name)
                .get()
                .await()

            val otherPendingIds = otherPendingSnapshot.documents
                .map { it.id }
                .filter { it != ownershipId }

            firestore.runTransaction { transaction ->
                val ownRef = firestore.collection("ownerships").document(ownershipId)
                val animRef = firestore.collection("animals").document(animalId)

                // Approve the selected request
                transaction.update(ownRef, "status", OwnershipStatus.APPROVED.name)

                // Mark animal as owned
                transaction.update(animRef, "status", "HASOWNED")

                // Reject all other pending requests for this animal
                otherPendingIds.forEach { otherId ->
                    val otherRef = firestore.collection("ownerships").document(otherId)
                    transaction.update(otherRef, "status", OwnershipStatus.REJECTED.name)
                }
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            val msg = StringHelper.getString(appContext, R.string.error_update_status)
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
                    val msg = StringHelper.getString(appContext, R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                firestore.collection("ownerships")
                    .document(ownershipId)
                    .update("status", OwnershipStatus.REJECTED.name)
                    .await()

                Result.success(Unit)

            } catch (e: Exception) {
                val msg = StringHelper.getString(appContext, R.string.error_update_status)
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
                return@withContext Result.failure(Exception("Without internet"))
            }

            try {
                val snapshot = firestore.collection("ownerships")
                    .whereEqualTo("shelterId", shelterId)
                    .whereEqualTo("status", OwnershipStatus.PENDING.name)
                    .get()
                    .await()

                if (snapshot.isEmpty) {
                    Log.w("DEBUG_SYNC", "AVISO: Nenhum pedido encontrado. Verifica se o shelterId no Firebase é EXATAMENTE '$shelterId' e se o status é 'PENDING'.")
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val item = doc.toOwnership()
                    if (item == null) {
                        Log.e("DEBUG_SYNC", "ERRO DE MAPPER: O documento '${doc.id}' falhou a conversão. Dados: ${doc.data}")
                    } else {
                        Log.d("DEBUG_SYNC", "Sucesso ao converter: ${item.id} - User: ${item.userId}")
                    }
                    item
                }
                ownershipDao.insertAll(list)

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

    suspend fun getPendingOwnershipsByShelterList(shelterId: String): List<Ownership> {
        return ownershipDao.getPendingOwnershipsByShelterList(shelterId)
    }

    /**
     * Gets an approved ownership that hasn't been celebrated yet.
     */
    suspend fun getUncelebratedApprovedOwnership(userId: String): Ownership? {
        return ownershipDao.getUncelebratedApprovedOwnership(userId)
    }

    /**
     * Marks ownership as celebrated both locally and in Firebase.
     */
    suspend fun markOwnershipAsCelebrated(ownershipId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // Update Firebase first
                if (NetworkUtils.isConnected()) {
                    firestore.collection("ownerships")
                        .document(ownershipId)
                        .update("celebrationShown", true)
                        .await()
                }

                // Update local database
                ownershipDao.markAsCelebrated(ownershipId)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("OwnershipRepository", "Error marking ownership as celebrated", e)
                Result.failure(e)
            }
        }

    /**
     * Syncs approved ownerships for a user, including celebration status.
     */
    suspend fun syncUserApprovedOwnershipsWithCelebration(userId: String) =
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
