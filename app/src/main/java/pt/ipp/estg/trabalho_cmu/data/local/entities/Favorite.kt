package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a user's favorite animal entry.
 *
 * Each Favorite links a user to an animal and is synchronized with Firebase.
 *
 * Properties:
 * @param id Primary key (matches Firebase document ID when synced)
 * @param userId ID of the user who marked the animal as favorite
 * @param animalId ID of the favorited animal
 * @param createdAt Timestamp of when the favorite was created (default: now)
 *
 * Constraints:
 * - ForeignKey to User (cascade delete)
 * - ForeignKey to Animal (cascade delete)
 * - Indexed by userId and animalId for efficient lookups
 */

@Entity(
    tableName = "favorites",
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
data class Favorite(
    @PrimaryKey
    val id: String = "",
    val userId: String,
    val animalId: String,
    val createdAt: Long = System.currentTimeMillis()
)
