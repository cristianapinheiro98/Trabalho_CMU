package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

fun User.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "address" to address,
        "email" to email,
        "phone" to phone
    )
}

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