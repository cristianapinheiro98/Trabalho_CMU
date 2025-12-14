package pt.ipp.estg.trabalho_cmu.data.models.mappers

import com.google.firebase.firestore.DocumentSnapshot
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk

/**
 * Mapper for converting Walk entities between Room and Firebase formats.
 *
 * Handles serialization and deserialization of Walk data including the
 * denormalized fields used for the SocialTails community feed.
 */
object WalkMapper {

    /**
     * Convert Walk entity to Firebase map format.
     *
     * Includes all walk data plus denormalized user and animal information
     * for efficient querying of the public walks feed.
     *
     * @return Map of walk data for Firebase storage
     */
    fun Walk.toFirebaseMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "animalId" to animalId,
            "date" to date,
            "startTime" to startTime,
            "endTime" to endTime,
            "duration" to duration,
            "distance" to distance,
            "routePoints" to routePoints,
            "medalType" to medalType,
            "createdAt" to createdAt,
            "isPublic" to isPublic,
            "userName" to userName,
            "animalName" to animalName,
            "animalImageUrl" to animalImageUrl
        )
    }

    /**
     * Convert Firebase DocumentSnapshot to Walk entity.
     *
     * Handles backwards compatibility for walks created before the
     * denormalized fields were added by using default values.
     *
     * @return Walk entity or null if conversion fails
     */
    fun DocumentSnapshot.toWalk(): Walk? {
        return try {
            Walk(
                id = id,
                userId = getString("userId") ?: return null,
                animalId = getString("animalId") ?: return null,
                date = getString("date") ?: return null,
                startTime = getString("startTime") ?: return null,
                endTime = getString("endTime") ?: return null,
                duration = getLong("duration") ?: 0L,
                distance = getDouble("distance") ?: 0.0,
                routePoints = getString("routePoints") ?: "",
                medalType = getString("medalType"),
                createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
                isPublic = getBoolean("isPublic") ?: false,
                userName = getString("userName") ?: "",
                animalName = getString("animalName") ?: "",
                animalImageUrl = getString("animalImageUrl") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}