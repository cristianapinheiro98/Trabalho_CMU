package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

// --- Escrita: Room Entity -> Firebase Map ---
// Usado quando vais criar um pedido novo no Firebase
fun Ownership.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "animalId" to animalId,
        "shelterId" to shelterId,
        "ownerName" to ownerName,
        "status" to status.name, // Enum para String
        "createdAt" to createdAt
        // Nota: Dados bancários sensíveis não são enviados aqui porque não existem na Entity
    )
}

// --- Leitura: Firebase Document -> Room Entity ---
// Usado quando descarregas pedidos do Firebase para guardar no Room
fun DocumentSnapshot.toOwnership(): Ownership? {
    return try {
        // Conversão segura do Status (String -> Enum)
        val statusStr = getString("status") ?: "PENDING"
        val statusEnum = try {
            OwnershipStatus.valueOf(statusStr)
        } catch (e: Exception) {
            OwnershipStatus.PENDING
        }

        Ownership(
            id = this.id, // ID do documento Firebase
            userId = getString("userId") ?: "",
            animalId = getString("animalId") ?: "",
            shelterId = getString("shelterId") ?: "",
            ownerName = getString("ownerName") ?: "",
            status = statusEnum,
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}