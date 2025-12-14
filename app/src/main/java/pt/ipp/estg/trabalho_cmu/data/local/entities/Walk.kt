package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Walk entity representing a completed walk with an animal.
 *
 * Contains all tracking data including route, duration, distance, and medal earned.
 * Supports public sharing to the SocialTails community with denormalized user and
 * animal information for efficient feed queries.
 *
 * @property id Unique identifier from Firebase
 * @property userId User who performed the walk
 * @property animalId Animal that was walked
 * @property date Date of the walk in format "dd/MM/yyyy"
 * @property startTime Start time in format "HH:mm:ss"
 * @property endTime End time in format "HH:mm:ss"
 * @property duration Total duration in seconds
 * @property distance Total distance in meters
 * @property routePoints GPS route as string "lat,lng;lat,lng;..."
 * @property medalType Medal earned: "BRONZE", "SILVER", "GOLD", or null
 * @property createdAt Timestamp of walk creation
 * @property isPublic Whether the walk is shared to SocialTails community
 * @property userName Name of the user who performed the walk (denormalized for feed)
 * @property animalName Name of the animal that was walked (denormalized for feed)
 * @property animalImageUrl First image URL of the animal (denormalized for feed)
 */
@Entity(
    tableName = "walks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["id"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["animalId"]),
        Index(value = ["isPublic"]),
        Index(value = ["createdAt"])
    ]
)
data class Walk(
    @PrimaryKey val id: String,
    val userId: String,
    val animalId: String,
    val date: String,               // "dd/MM/yyyy"
    val startTime: String,          // "HH:mm:ss"
    val endTime: String,            // "HH:mm:ss"
    val duration: Long,             // in seconds
    val distance: Double,           // in meters
    val routePoints: String,        // "lat,lng;lat,lng;..."
    val medalType: String?,         // "BRONZE", "SILVER", "GOLD", null
    val createdAt: Long,
    val isPublic: Boolean = false,
    val userName: String = "",
    val animalName: String = "",
    val animalImageUrl: String = ""
)
