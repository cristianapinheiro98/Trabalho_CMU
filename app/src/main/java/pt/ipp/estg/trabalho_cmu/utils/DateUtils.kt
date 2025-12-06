package pt.ipp.estg.trabalho_cmu.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formato padrão usado no Firebase e na app
private const val DATE_FORMAT = "dd/MM/yyyy"
private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.US)

/**
 * Converte String (dd/MM/yyyy) para Long (timestamp)
 */
fun dateStringToLong(dateString: String): Long {
    return try {
        dateFormatter.parse(dateString)?.time ?: 0L
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

/**
 * Converte Long (timestamp) para String (dd/MM/yyyy)
 */
fun longToDateString(timestamp: Long): String {
    return try {
        dateFormatter.format(Date(timestamp))
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
