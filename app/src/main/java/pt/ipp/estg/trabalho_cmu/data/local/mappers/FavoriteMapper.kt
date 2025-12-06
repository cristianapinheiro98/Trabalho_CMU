package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite

fun Favorite.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "animalId" to animalId,
        "createdAt" to createdAt
    )
}

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