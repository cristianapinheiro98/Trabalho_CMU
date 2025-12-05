package pt.ipp.estg.trabalho_cmu.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun dateStringToLong(dateString: String): Long {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .parse(dateString)
        ?.time ?: 0L
}
