package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Represents a "special adoption" (ownership) request.
 * It includes payment details, user/animal/shelter relations, and a status
 * that can later be updated by an admin (Pending, Approved, Rejected).
 */
@Entity(
    tableName = "OwnershipRequests"
/*,
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
    ]*/
)
data class OwnershipRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Foreign Keys
    val userId: String,
    val animalId: String,
    val shelterId: String,

    // Payment and identification info
    val ownerName: String,
    val accountNumber: String,
    val cvv: String,
    val cardNumber: String,
    val password: String,

    // Ownership status
    @ColumnInfo(defaultValue = "PENDING")
    val status: OwnershipStatus = OwnershipStatus.PENDING,

    // Terms acceptance and timestamp
    val acceptedTerms: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
