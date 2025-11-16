package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "veterinarians")
data class Veterinarian(
    @PrimaryKey
    val placeId: String, // Google Place ID
    val name: String,
    val address: String,
    val phone: String,
    val isOpenNow: Boolean,
    val todaySchedule: String,
    val weekdaySchedules: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double? = null, // Google rating
    val cachedAt: Long = System.currentTimeMillis() // for a 24h TTL (time to live)
)