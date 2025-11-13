package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus

/**
 * Represents a "special adoption" (ownership) request.
 * It includes payment details, user/animal/shelter relations, and a status
 * that can later be updated by an admin (Pending, Approved, Rejected).
 */
@Entity(
    tableName = "OwnershipRequests"
,
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
        Index("userId"),
        Index("animalId"),
        Index("shelterId")
    ]
)
data class Ownership(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Foreign Keys
    val userId: Int,
    val animalId: Int,
    val shelterId: Int,

    // Payment and identification info
    val ownerName: String,
    val accountNumber: String,
    val cvv: String,
    val cardNumber: String,

    // Ownership status
    @ColumnInfo(defaultValue = "PENDING")
    val status: OwnershipStatus = OwnershipStatus.PENDING,

    val createdAt: Long = System.currentTimeMillis()
)