package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

/**
 * Represents a "special adoption" (ownership) request.
 * It includes payment details, user/animal/shelter relations, and a status
 * that can later be updated by an admin (Pending, Approved, Rejected).
 */
@Entity(
    tableName = "OwnershipRequests",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["firebaseUid"],
            childColumns = ["userFirebaseUid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["firebaseUid"],
            childColumns = ["animalFirebaseUid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Shelter::class,
            parentColumns = ["firebaseUid"],
            childColumns = ["shelterFirebaseUid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userFirebaseUid"]),
        Index(value = ["animalFirebaseUid"]),
        Index(value = ["shelterFirebaseUid"])
    ]
)
data class Ownership(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firebaseUid: String? = null,

    // Foreign Keys
    val userFirebaseUid: String,
    val animalFirebaseUid: String,
    val shelterFirebaseUid: String,

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