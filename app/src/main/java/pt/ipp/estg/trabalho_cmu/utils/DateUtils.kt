package pt.ipp.estg.trabalho_cmu.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for converting between formatted date strings and
 * Unix timestamps (Long values).
 *
 * Supported format: **dd/MM/yyyy**
 *
 * Functions:
 * - [dateStringToLong]: Converts a date string in the format *dd/MM/yyyy* into
 *   a Unix timestamp (milliseconds since epoch). Returns 0L if parsing fails.
 * - [longToDateString]: Converts a Unix timestamp into a formatted string
 *   following the *dd/MM/yyyy* pattern. Returns an empty string if formatting fails.
 *
 * These functions rely on a shared [SimpleDateFormat] instance with US locale,
 * making the conversions consistent throughout the application.
 */
private const val DATE_FORMAT = "dd/MM/yyyy"
private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.US)

fun dateStringToLong(dateString: String): Long {
    return try {
        dateFormatter.parse(dateString)?.time ?: 0L
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

fun longToDateString(timestamp: Long): String {
    return try {
        dateFormatter.format(Date(timestamp))
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
