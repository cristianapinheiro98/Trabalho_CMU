package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus


/**
 * Entity representing an ownership/adoption request stored in the local Room database.
 *
 * Each Ownership record links:
 * - a User requesting the adoption,
 * - an Animal being requested,
 * - and the Shelter responsible for that animal.
 *
 * This entity enforces referential integrity with three foreign keys:
 * - If a User is deleted, all their requests are removed (CASCADE).
 * - If an Animal is deleted, related requests are removed (CASCADE).
 * - If a Shelter is deleted, its related requests are removed (CASCADE).
 *
 * @property id Unique identifier of the ownership request (matches remote Firebase ID).
 * @property userId Foreign key referencing the user who submitted the request.
 * @property animalId Foreign key referencing the requested animal.
 * @property shelterId Foreign key referencing the shelter responsible for the animal.
 * @property status Current status of the request (PENDING, APPROVED, REJECTED).
 * @property createdAt Timestamp representing when the request was created.
 */
@Entity(
    tableName = "OwnershipRequests",
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
        ),
        ForeignKey(
            entity = Shelter::class,
            parentColumns = ["id"],
            childColumns = ["shelterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["animalId"]),
        Index(value = ["shelterId"])
    ]
)
data class Ownership(
    @PrimaryKey
    val id: String,
    val userId: String,
    val animalId: String,
    val shelterId: String,
    val status: OwnershipStatus = OwnershipStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val celebrationShown: Boolean = false
)