package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*


/**
 * Interactive calendar component that allows selecting multiple dates.
 *
 * Features:
 * - Displays the month header
 * - Shows weekday initials using localized strings
 * - Highlights selected dates
 * - Calls onDateSelected() when the user toggles a day
 */
@Composable
fun CalendarView(
    selectedDates: Set<String>,
    onDatesChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCalendar = remember { Calendar.getInstance() }
    var startDate by remember { mutableStateOf<String?>(null) }
    var endDate by remember { mutableStateOf<String?>(null) }
    val year = currentCalendar.get(Calendar.YEAR)
    val month = currentCalendar.get(Calendar.MONTH)

    // Primeiro dia do mês
    val firstDayCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    fun fillDateRange(start: String, end: String): Set<String> {
        val dates = mutableSetOf<String>()
        val startParts = start.split("/")
        val endParts = end.split("/")

        val startDay = startParts[0].toInt()
        val endDay = endParts[0].toInt()
        val month = startParts[1].toInt()
        val year = startParts[2].toInt()

        for (day in startDay..endDay) {
            dates.add("$day/$month/$year")
        }
        return dates
    }

    val daysInMonth = firstDayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = firstDayCalendar.get(Calendar.DAY_OF_WEEK) - 1

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Cabeçalho do mês
        Text(
            text = monthFormat.format(currentCalendar.time).replaceFirstChar { it.uppercase() },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2C),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        // Dias da semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF757575),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dias do mês
        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val currentDay = dayCounter
                        val dateString = "$currentDay/${month + 1}/${year % 100}"
                        val isSelected = selectedDates.contains(dateString)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF2C8B7E) else Color.Transparent
                                )
                                .clickable {
                                    when {
                                        startDate == null -> {
                                            startDate = dateString
                                            endDate = null
                                            onDatesChanged(emptySet())
                                        }

                                        endDate == null -> {
                                            val startDay = startDate!!.split("/")[0].toInt()
                                            val clickedDay = currentDay

                                            if (clickedDay >= startDay) {
                                                endDate = dateString
                                                val range = fillDateRange(startDate!!, dateString)
                                                onDatesChanged(range)
                                            } else {
                                                endDate = startDate
                                                startDate = dateString
                                                val range = fillDateRange(dateString, endDate!!)
                                                onDatesChanged(range)
                                            }
                                        }

                                        else -> {
                                            startDate = dateString
                                            endDate = null
                                            onDatesChanged(emptySet())
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color(0xFF2C2C2C)
                            )
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}