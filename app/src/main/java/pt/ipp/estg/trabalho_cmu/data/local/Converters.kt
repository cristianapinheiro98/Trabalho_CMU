package pt.ipp.estg.trabalho_cmu.data.local

import androidx.room.TypeConverter
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus
import java.util.Date

class Converters {

    // Converter para Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

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
}