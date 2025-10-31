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
}