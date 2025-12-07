package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

/**
 * Converts a local Ownership entity into a Firebase map.
 *
 * Used when uploading adoption/ownership requests to Firestore.
 */
fun Ownership.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "animalId" to animalId,
        "shelterId" to shelterId,
        "status" to status.name,
        "createdAt" to createdAt
    )
}

/**
 * Converts a Firebase DocumentSnapshot into a local Ownership entity.
 *
 * Includes safety checks to ensure invalid enum values do not break parsing.
 *
 * @receiver DocumentSnapshot from Firestore
 * @return Ownership? or null if parsing fails
 */
fun DocumentSnapshot.toOwnership(): Ownership? {
    return try {
        val statusStr = getString("status") ?: "PENDING"
        val statusEnum = try {
            OwnershipStatus.valueOf(statusStr)
        } catch (e: Exception) {
            OwnershipStatus.PENDING
        }

        Ownership(
            id = this.id,
            userId = getString("userId") ?: "",
            animalId = getString("animalId") ?: "",
            shelterId = getString("shelterId") ?: "",
            status = statusEnum,
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
