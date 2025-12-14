package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a veterinary center cached locally in Room.
 *
 * Data is fetched from Google Places API and stored with a TTL (time-to-live)
 * to enable offline access and reduce API calls.
 *
 * @property placeId Unique identifier from Google Places API.
 * @property name Name of the veterinary center.
 * @property address Physical address of the location.
 * @property phone Contact phone number (or fallback message if unavailable).
 * @property isOpenNow Whether the location is currently open.
 * @property todaySchedule Today's opening hours (e.g., "Monday: 9:00 AM – 6:00 PM").
 * @property weekdaySchedules Full week schedule separated by "|".
 * @property latitude Geographic latitude coordinate.
 * @property longitude Geographic longitude coordinate.
 * @property rating Google Places rating (0.0 to 5.0), null if not available.
 * @property cachedAt Timestamp when the record was cached, used for 24h TTL validation.
 */
@Entity(tableName = "veterinarians")
data class Veterinarian(
    @PrimaryKey
    val placeId: String,
    val name: String,
    val address: String,
    val phone: String,
    val isOpenNow: Boolean,
    val todaySchedule: String,
    val weekdaySchedules: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double? = null,
    val cachedAt: Long = System.currentTimeMillis()
)