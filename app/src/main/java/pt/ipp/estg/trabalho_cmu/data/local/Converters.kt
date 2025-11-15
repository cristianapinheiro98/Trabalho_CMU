package pt.ipp.estg.trabalho_cmu.data.local

import androidx.room.TypeConverter
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus
import java.util.Date

/**
 * A collection of [TypeConverter] methods that allow Room to persist
 * complex data types that it does not natively support.
 *
 * This class includes converters for [Date], custom enums like [OwnershipStatus]
 * and [AnimalStatus], and lists of strings. These converters are registered
 * with the [AppDatabase].
 */
class Converters {

    /**
     * Converts a Long timestamp (milliseconds since epoch) into a [Date] object.
     * Room uses this to read from the database.
     *
     * @param value The Long timestamp from the database. Can be null.
     * @return A [Date] object, or `null` if the input value is null.
     */
    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts a [Date] object into a Long timestamp (milliseconds since epoch).
     * Room uses this to write to the database.
     *
     * @param date The [Date] object to convert. Can be null.
     * @return A Long timestamp, or `null` if the input date is null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }


    /**
     * Converts an [OwnershipStatus] enum into its String representation (its name).
     * This allows Room to store the enum as a plain string.
     *
     * @param status The [OwnershipStatus] enum instance.
     * @return The string name of the enum (e.g., "PENDING").
     */
    @TypeConverter
    fun fromOwnershipStatus(status: OwnershipStatus): String {
        return status.name
    }

    /**
     * Converts a String back into an [OwnershipStatus] enum.
     * Room uses this to reconstruct the enum object when reading from the database.
     *
     * @param value The string representation of the status from the database.
     * @return The corresponding [OwnershipStatus] enum instance.
     */
    @TypeConverter
    fun toOwnershipStatus(value: String): OwnershipStatus {
        return OwnershipStatus.valueOf(value)
    }

    /**
     * Converts an [AnimalStatus] enum into its String representation (its name).
     * This allows Room to store the enum as a plain string.
     *
     * @param status The [AnimalStatus] enum instance.
     * @return The string name of the enum (e.g., "AVAILABLE").
     */
    @TypeConverter
    fun fromAnimalStatus(status: AnimalStatus): String {
        return status.name
    }

    /**
     * Converts a String back into an [AnimalStatus] enum.
     * Room uses this to reconstruct the enum object when reading from the database.
     *
     * @param value The string representation of the status from the database.
     * @return The corresponding [AnimalStatus] enum instance.
     */
    @TypeConverter
    fun toAnimalStatus(value: String): AnimalStatus {
        return AnimalStatus.valueOf(value)
    }

    /**
     * Converts a list of strings into a single delimited string.
     * This allows Room to store a list of strings in a single text column.
     * The delimiter used is "|".
     *
     * @param list The list of strings to be converted.
     * @return A single string representation of the list, or an empty string if the list is null.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString("|") ?: ""
    }

    /**
     * Converts a single delimited string back into a list of strings.
     * Room uses this to reconstruct the list when reading from the database.
     * It splits the string by the "|" delimiter.
     *
     * @param value The delimited string from the database.
     * @return A list of strings. Returns an empty list if the input value is null or empty.
     */
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split("|")
    }
}
