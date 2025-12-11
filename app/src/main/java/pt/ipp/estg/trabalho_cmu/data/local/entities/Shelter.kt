package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity describing a shelter stored locally in the Room database.
 *
 * A shelter represents an organization responsible for hosting animals.
 * Each shelter may have multiple animals associated with it.
 *
 * @property id Unique identifier of the shelter (matches remote Firebase ID).
 * @property name Official name of the shelter.
 * @property address Physical address of the shelter.
 * @property phone Contact phone number.
 * @property email Official email address.
 * @property openingTime Opening time in HH:mm format (e.g., "09:00"). Can be null if not set.
 * @property closingTime Closing time in HH:mm format (e.g., "18:00"). Can be null if not set.
 */
@Entity(tableName = "shelters")
data class Shelter(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val openingTime: String? = null,
    val closingTime: String? = null
)
