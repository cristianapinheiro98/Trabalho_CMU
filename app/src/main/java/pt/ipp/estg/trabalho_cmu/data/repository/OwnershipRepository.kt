package pt.ipp.estg.trabalho_cmu.data.repository

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toOwnership
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap

class OwnershipRepository(
    private val ownershipDao: OwnershipDao,
    private val application: Application
) {
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore

    // --- LEITURA ---
    fun getPendingOwnershipsByUser(userId: String) = ownershipDao.getPendingOwnershipsByUser(userId)
    fun getPendingOwnershipsByShelter(shelterId: String) = ownershipDao.getPendingOwnershipsByShelter(shelterId)

    suspend fun getOwnershipById(id: String) = ownershipDao.getOwnershipById(id)

    // --- CREATE (Firebase Only) ---
    suspend fun createOwnership(ownership: Ownership): Result<Ownership> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))

            // Validar duplicados locais antes de ir à net
            if (ownershipDao.getExistingRequest(ownership.userId, ownership.animalId) != null) {
                return@withContext Result.failure(Exception("Pedido duplicado."))
            }

            // Usa o mapper externo 'toFirebaseMap()'
            val docRef = firestore.collection("ownerships").add(ownership.toFirebaseMap()).await()
            val savedOwnership = ownership.copy(id = docRef.id)

            Result.success(savedOwnership)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- ACTIONS (Firebase Only) ---
    suspend fun approveOwnershipRequest(ownershipId: String, animalId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))

            firestore.runTransaction { transaction ->
                val ownRef = firestore.collection("ownerships").document(ownershipId)
                val animRef = firestore.collection("animals").document(animalId)
                transaction.update(ownRef, "status", OwnershipStatus.APPROVED.name)
                transaction.update(animRef, "status", "HASOWNED")
            }.await()

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rejectOwnershipRequest(ownershipId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))

            firestore.collection("ownerships").document(ownershipId)
                .update("status", OwnershipStatus.REJECTED.name).await()

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- SYNC (Firebase -> Room) ---
    suspend fun syncPendingOwnerships(shelterId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))

        try {
            val snapshot = firestore.collection("ownerships")
                .whereEqualTo("shelterId", shelterId)
                .whereEqualTo("status", OwnershipStatus.PENDING.name)
                .get().await()

            // Usa o mapper externo 'toOwnership()'
            val list = snapshot.documents.mapNotNull { it.toOwnership() }

            ownershipDao.refreshCache(list)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // 1. Buscar localmente
    suspend fun getApprovedOwnershipsByUser(userId: String) = ownershipDao.getApprovedOwnershipsByUser(userId)

    // 2. Sincronizar da Cloud (Importante para Online-First)
    suspend fun syncUserApprovedOwnerships(userId: String) = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext

        try {
            val snapshot = firestore.collection("ownerships")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", OwnershipStatus.APPROVED.name)
                .get().await()

            val list = snapshot.documents.mapNotNull { it.toOwnership() }

            // Insere/Atualiza na cache (não apaga os pendentes, apenas insere estes)
            ownershipDao.insertAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}