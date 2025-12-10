package pt.ipp.estg.trabalho_cmu.data.local

import androidx.room.TypeConverter
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

/**
 * Type converters required by Room to store custom types such as enums
 * and lists inside the SQLite database.
 *
 * This class provides bidirectional conversion between:
 * - OwnershipStatus ↔ String
 * - AnimalStatus ↔ String
 * - List<String> ↔ String (stored as a pipe-separated list)
 */
class Converters {

    /** Converts OwnershipStatus enum to a Storable String */
    @TypeConverter
    fun fromOwnershipStatus(status: OwnershipStatus): String {
        return status.name
    }

    /** Converts stored String back into OwnershipStatus enum */
    @TypeConverter
    fun toOwnershipStatus(value: String): OwnershipStatus {
        return OwnershipStatus.valueOf(value)
    }

    /** Converts AnimalStatus enum to a String */
    @TypeConverter
    fun fromAnimalStatus(status: AnimalStatus): String {
        return status.name
    }

    /** Converts String to AnimalStatus enum */
    @TypeConverter
    fun toAnimalStatus(value: String): AnimalStatus {
        return AnimalStatus.valueOf(value)
    }

    /**
     * Converts a list of Strings into a single String separated by '|'.
     * Used for storing image URLs in the database.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString("|") ?: ""
    }

    /**
     * Converts a pipe-separated String back into a List<String>.
     */
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|")
    }
}
