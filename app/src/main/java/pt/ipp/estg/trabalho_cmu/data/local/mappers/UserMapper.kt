package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

/**
 * Converts a local User entity into a Firebase map.
 *
 * Used when uploading or updating user profile information.
 */
fun User.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "address" to address,
        "email" to email,
        "phone" to phone
    )
}

/**
 * Converts a Firestore DocumentSnapshot into a local User entity.
 *
 * @return User? or null if an error occurs.
 */
fun DocumentSnapshot.toUser(): User? {
    return try {
        User(
            id = this.id,
            name = getString("name") ?: "",
            address = getString("address") ?: "",
            email = getString("email") ?: "",
            phone = getString("phone") ?: ""
        )
    } catch (e: Exception) {
        null
    }
}
