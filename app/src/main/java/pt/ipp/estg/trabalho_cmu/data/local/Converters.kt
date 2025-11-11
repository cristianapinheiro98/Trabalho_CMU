package pt.ipp.estg.trabalho_cmu.data.local

import androidx.room.TypeConverter
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus
import java.util.Date

class Converters {

    // Converte Long (timestamp) de volta para Date
    @TypeConverter
    fun TimestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // Converte a data para Long (timestamp)
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Converter para Enum OwnershipStatus
    @TypeConverter
    fun fromOwnershipStatus(status: OwnershipStatus): String {
        return status.name
    }

    @TypeConverter
    fun toOwnershipStatus(status: String): OwnershipStatus {
        return OwnershipStatus.valueOf(status)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        // Check if the value is null or empty, return an empty list if so.
        if (value.isNullOrEmpty()) {
            return emptyList()
        }
        // Split the string by a comma and convert each part to an Integer.
        return value.split(",").map { it.trim().toInt() }
    }

    @TypeConverter
    fun fromIntList(list: List<Integer>?): String? {
        // Join the list of integers into a single comma-separated string.
        return list?.joinToString(",")
    }
}
