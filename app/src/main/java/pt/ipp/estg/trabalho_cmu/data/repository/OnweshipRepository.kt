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

open class OwnershipRepository(
    private val ownershipDao: OwnershipDao,
    private val firestore: FirebaseFirestore
) {

    open fun getOwnershipsByUser(userId: Int): LiveData<List<Ownership>> =
        ownershipDao.getOwnershipsByUser(userId)

    suspend fun getOwnershipById(ownershipId: Int): Ownership? =
        ownershipDao.getOwnershipById(ownershipId)

    // ========= CREATE OWNERSHIP (Firebase + Room) =========
    suspend fun createOwnership(ownership: Ownership): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Insere no Room primeiro para obter o ID
            val generatedId = ownershipDao.insertOwnership(ownership).toInt()

            // 2. Cria ownership com o ID correto
            val ownershipWithId = ownership.copy(id = generatedId)

            // 3. Prepara dados para Firebase (SEM dados sensíveis de cartão!)
            val data = hashMapOf(
                "id" to generatedId,
                "userId" to ownershipWithId.userId,
                "animalId" to ownershipWithId.animalId,
                "shelterId" to ownershipWithId.shelterId,
                "ownerName" to ownershipWithId.ownerName,
                // NÃO guardar dados do cartão no Firebase por segurança!
                // "accountNumber" to ownershipWithId.accountNumber,
                // "cvv" to ownershipWithId.cvv,
                // "cardNumber" to ownershipWithId.cardNumber,
                "status" to ownershipWithId.status.name,
                "createdAt" to ownershipWithId.createdAt
            )

            // 4. Guarda no Firebase com o ID do Room
            firestore.collection("ownerships")
                .document(generatedId.toString())
                .set(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========= FETCH FROM FIREBASE =========
    suspend fun fetchOwnerships() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("ownerships").get().await()

            val ownerships = snapshot.documents.mapNotNull { it.toOwnership() }

            if (ownerships.isNotEmpty()) {
                ownershipDao.insertAll(ownerships)
            }
        } catch (e: Exception) {
            println("Erro ao buscar ownerships do Firebase: ${e.message}")
        }
    }

    // ========= UPDATE STATUS (Firebase + Room) =========
    open suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        try {
            // Atualiza no Room
            ownershipDao.updateOwnershipStatus(id, status)

            // Atualiza no Firebase
            firestore.collection("ownerships")
                .document(id.toString())
                .update("status", status.name)
                .await()
        } catch (e: Exception) {
            println("Erro ao atualizar status: ${e.message}")
            throw e
        }
    }

    // ========= DELETE (Firebase + Room) =========
    open suspend fun deleteOwnership(ownership: Ownership) {
        try {
            // Remove do Room
            ownershipDao.deleteOwnership(ownership)

            // Remove do Firebase
            firestore.collection("ownerships")
                .document(ownership.id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            println("Erro ao eliminar ownership: ${e.message}")
            throw e
        }
    }

    // ========= CONVERTER FIREBASE → OWNERSHIP =========
    private fun DocumentSnapshot.toOwnership(): Ownership? = try {
        Ownership(
            id = (getLong("id") ?: 0).toInt(),
            userId = (getLong("userId") ?: 0).toInt(),
            animalId = (getLong("animalId") ?: 0).toInt(),
            shelterId = (getLong("shelterId") ?: 0).toInt(),
            ownerName = getString("ownerName") ?: "",
            accountNumber = "", // Não vem do Firebase por segurança
            cvv = "", // Não vem do Firebase por segurança
            cardNumber = "", // Não vem do Firebase por segurança
            status = OwnershipStatus.valueOf(getString("status") ?: "PENDING"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        println("Erro ao converter ownership: ${e.message}")
        null
    }
}