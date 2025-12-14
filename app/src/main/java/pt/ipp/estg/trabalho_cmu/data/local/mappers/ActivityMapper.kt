package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity

/**
 * Converts a local Activity entity into a Firebase-compatible map.
 *
 * This is used when uploading or syncing activity data to Firestore.
 *
 * @return Map<String, Any> representing the fields stored in Firebase.
 */
fun Activity.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "animalId" to animalId,
        "pickupDate" to pickupDate,
        "pickupTime" to pickupTime,
        "deliveryDate" to deliveryDate,
        "deliveryTime" to deliveryTime,
        "createdAt" to createdAt
    )
}

/**
 * Converts a Firestore DocumentSnapshot into a local Activity entity.
 *
 * This is used when downloading or syncing activity data from Firestore.
 *
 * @receiver DocumentSnapshot received from Firestore
 * @return Activity? object, or null if parsing fails
 */
fun DocumentSnapshot.toActivity(): Activity? {
    return try {
        Activity(
            id = this.id,
            userId = getString("userId") ?: "",
            animalId = getString("animalId") ?: "",
            pickupDate = getString("pickupDate") ?: "",
            pickupTime = getString("pickupTime") ?: "",
            deliveryDate = getString("deliveryDate") ?: "",
            deliveryTime = getString("deliveryTime") ?: "",
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}