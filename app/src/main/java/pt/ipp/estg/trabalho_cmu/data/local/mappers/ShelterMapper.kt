package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

// --- Escrita: Room Entity -> Firebase Map ---
fun Shelter.toFirebaseMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "address" to address,
        "contact" to phone,
        "email" to email
    )
}

// --- Leitura: Firebase Document -> Room Entity ---
fun DocumentSnapshot.toShelter(): Shelter? {
    return try {
        Shelter(
            id = this.id, // ID do documento Firebase
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