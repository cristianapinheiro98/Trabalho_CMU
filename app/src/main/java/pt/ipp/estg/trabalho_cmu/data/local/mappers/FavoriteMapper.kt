package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite


/**
 * Converts a Favorite Room entity into a Firestore-compatible map.
 *
 * Used when syncing local favorites to the Firebase database.
 */
fun Favorite.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "animalId" to animalId,
        "createdAt" to createdAt
    )
}


/**
 * Converts a Firestore document into a Favorite entity.
 *
 * Returns null if required fields are missing or an exception occurs.
 * Used when synchronizing favorites from Firebase back to Room.
 */
fun DocumentSnapshot.toFavorite(): Favorite? {
    return try {
        Favorite(
            id = this.id,
            userId = getString("userId") ?: "",
            animalId = getString("animalId") ?: "",
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}