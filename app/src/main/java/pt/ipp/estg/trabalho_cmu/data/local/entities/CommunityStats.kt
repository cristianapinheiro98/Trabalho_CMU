package pt.ipp.estg.trabalho_cmu.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Entity representing a user's progress and ranking status in the community.
 * Tracks points, total distance walked, and references the animal involved.
 */
@Entity(
    tableName = "community_stats",
    foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["animalId"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.SET_NULL // If animal is deleted, set animalId to null
        )
    ]
)
data class CommunityStats(
    // The primary key will be the userId to ensure only one stats entry per user
    @PrimaryKey
    val userId: String,
    val animalId: String? = null,   // Reference to the user's current animal
    val currentLevel: Int = 1,      // User's community level
    val totalPoints: Int = 0,       // Points accumulated for ranking
    val totalKm: Float = 0f,        // Total distance walked with the animal
    val lastUpdated: Long = System.currentTimeMillis()
)
