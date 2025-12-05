package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity

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