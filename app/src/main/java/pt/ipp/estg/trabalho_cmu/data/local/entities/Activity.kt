package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an activity stored locally in the Room database.
 *
 * This table includes foreign keys referencing both the User and Animal entities,
 * ensuring referential integrity. When a referenced user or animal is deleted,
 * associated activities are automatically removed due to CASCADE delete.
 *
 * @property id Unique identifier of the activity.
 * @property userId Foreign key referencing the user participating in the activity.
 * @property animalId Foreign key referencing the animal involved in the activity.
 * @property pickupDate Date when the animal is picked up from the shelter.
 * @property pickupTime Time of the pickup.
 * @property deliveryDate Date when the animal is delivered or returned.
 * @property deliveryTime Time of the delivery.
 * @property createdAt Timestamp representing when the record was created locally.
 */
@Entity(
    tableName = "activities",
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
        Index(value = ["animalId"])
    ]
)
data class Activity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val animalId: String,
    val pickupDate: String,
    val pickupTime: String,
    val deliveryDate: String,
    val deliveryTime: String,
    val createdAt: Long = System.currentTimeMillis()
)
