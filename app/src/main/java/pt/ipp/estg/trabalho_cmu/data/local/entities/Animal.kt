package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

/**
 * Entity representing an animal stored locally in the Room database.
 *
 * This table includes a foreign key referencing the Shelter entity,
 * ensuring referential integrity. When a shelter is deleted, all associated
 * animals are automatically removed due to CASCADE delete.
 *
 * @property id Unique identifier of the animal (matches remote Firebase ID).
 * @property name Animal name.
 * @property breed Animal breed.
 * @property species Species of the animal (e.g., dog, cat).
 * @property size Size category (e.g., small, medium, large).
 * @property birthDate Birth date stored as a timestamp in milliseconds.
 * @property imageUrls List of image URLs stored for this animal.
 * @property description Additional text describing the animal.
 * @property shelterId Foreign key referencing the shelter responsible for the animal.
 * @property status Availability status (AVAILABLE, ADOPTED, etc.).
 * @property createdAt Timestamp representing when the record was created locally.
 */
@Entity(
    tableName = "animals",
    foreignKeys = [
        ForeignKey(
            entity = Shelter::class,
            parentColumns = ["id"],
            childColumns = ["shelterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shelterId"])]
)
data class Animal(
    @PrimaryKey
    val id: String,
    val name: String,
    val breed: String,
    val species: String,
    val size: String,
    val birthDate: Long,
    val imageUrls: List<String>,
    val description: String,
    val shelterId: String,
    val status: AnimalStatus = AnimalStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis()
)
