package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

/**
 * Converts a local Shelter entity into a Firebase map.
 *
 * Used when syncing shelter information to Firestore.
 */
fun Shelter.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "address" to address,
        "contact" to phone,
        "email" to email
    )
}

/**
 * Converts a Firestore DocumentSnapshot into a local Shelter entity.
 *
 * @return Shelter? parsed object or null if an error occurs.
 */
fun DocumentSnapshot.toShelter(): Shelter? {
    return try {
        Shelter(
            id = this.id,
            name = getString("name") ?: "",
            address = getString("address") ?: "",
            phone = getString("contact") ?: "",
            email = getString("email") ?: ""
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
