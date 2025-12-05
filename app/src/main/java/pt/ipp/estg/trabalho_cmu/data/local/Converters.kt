package pt.ipp.estg.trabalho_cmu.data.local

import androidx.room.TypeConverter
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus
import java.util.Date

class Converters {


    // --------------------------
    // OWNERSHIP STATUS ENUM
    // --------------------------
    @TypeConverter
    fun fromOwnershipStatus(status: OwnershipStatus): String {
        return status.name
    }

    @TypeConverter
    fun toOwnershipStatus(value: String): OwnershipStatus {
        return OwnershipStatus.valueOf(value)
    }

    // --------------------------
    // ANIMAL STATUS ENUM
    // --------------------------
    @TypeConverter
    fun fromAnimalStatus(status: AnimalStatus): String {
        return status.name
    }

    @TypeConverter
    fun toAnimalStatus(value: String): AnimalStatus {
        return AnimalStatus.valueOf(value)
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString("|") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|")
    }
}
