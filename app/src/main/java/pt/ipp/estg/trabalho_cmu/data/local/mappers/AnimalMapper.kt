package pt.ipp.estg.trabalho_cmu.data.local.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong
import pt.ipp.estg.trabalho_cmu.utils.longToDateString

// --- Escrita: Room Entity -> Firebase Map ---
fun Animal.toFirebaseMap(): Map<String, Any> {

    return mapOf(
        "name" to name,
        "breed" to breed,
        "species" to species,
        "size" to size,
        "birthDate" to longToDateString(birthDate),
        "imageUrls" to imageUrls, // O Firebase aceita listas nativamente
        "description" to description,
        "shelterId" to shelterId,
        "status" to status.name, // Guarda o Enum como String
        "createdAt" to createdAt
    )
}

// --- Leitura: Firebase Document -> Room Entity ---
fun DocumentSnapshot.toAnimal(): Animal? {
    return try {
        val dateStr = getString("birthDate") ?: ""
        val birthDateLong = dateStringToLong(dateStr)

        Animal(
            id = this.id, // O ID vem do nome do documento no Firebase
            name = getString("name") ?: "",
            breed = getString("breed") ?: "",
            species = getString("species") ?: "",
            size = getString("size") ?: "",
            birthDate = birthDateLong,
            // Cast seguro para lista de strings
            imageUrls = (get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            description = getString("description") ?: "",
            shelterId = getString("shelterId") ?: "",
            // Converte String de volta para Enum
            status = AnimalStatus.valueOf(getString("status")?.uppercase() ?: "AVAILABLE"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}