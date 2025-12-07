package pt.ipp.estg.trabalho_cmu.data.local.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong
import pt.ipp.estg.trabalho_cmu.utils.longToDateString

/**
 * Converts a local Animal entity into a Firebase-compatible map.
 *
 * This is used when uploading or syncing animal data to Firestore.
 *
 * @return Map<String, Any> representing the fields stored in Firebase.
 */
fun Animal.toFirebaseMap(): Map<String, Any> {

    return mapOf(
        "name" to name,
        "breed" to breed,
        "species" to species,
        "size" to size,
        "birthDate" to longToDateString(birthDate),
        "imageUrls" to imageUrls,
        "description" to description,
        "shelterId" to shelterId,
        "status" to status.name,
        "createdAt" to createdAt
    )
}

/**
 * Converts a Firestore DocumentSnapshot into a local Animal entity.
 *
 * This is used when downloading or syncing animal data from Firestore.
 *
 * @receiver DocumentSnapshot received from Firestore
 * @return Animal? object, or null if parsing fails
 */
fun DocumentSnapshot.toAnimal(): Animal? {
    return try {
        val dateStr = getString("birthDate") ?: ""
        val birthDateLong = dateStringToLong(dateStr)

        Animal(
            id = this.id,
            name = getString("name") ?: "",
            breed = getString("breed") ?: "",
            species = getString("species") ?: "",
            size = getString("size") ?: "",
            birthDate = birthDateLong,
            imageUrls = (get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            description = getString("description") ?: "",
            shelterId = getString("shelterId") ?: "",
            status = AnimalStatus.valueOf(getString("status")?.uppercase() ?: "AVAILABLE"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
