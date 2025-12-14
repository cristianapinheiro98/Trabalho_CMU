package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * - Highlights selected dates in green
 * - Shows booked dates in gray (non-clickable)
 * - Calls onDateClicked() when the user clicks a day
 *
 * @param selectedDates Set of dates currently selected (format: "dd/MM/yyyy")
 * @param onDateClicked Callback invoked when user clicks a date
 * @param bookedDates List of dates that are already booked (format: "dd/MM/yyyy")
 * @param modifier Modifier for this composable
 */
@Composable
fun CalendarView(
    selectedDates: Set<String>,
    onDateClicked: (String) -> Unit,
    bookedDates: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    android.util.Log.d("CalendarView", "Received bookedDates: $bookedDates")

    val currentCalendar = remember { Calendar.getInstance() }
    val year = currentCalendar.get(Calendar.YEAR)
    val month = currentCalendar.get(Calendar.MONTH)

    // First day of the month
    val firstDayCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
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
        // Month header
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

        // Days of the week
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
                        val dateString = String.format("%02d/%02d/%d", currentDay, month + 1, year)
                        val isSelected = selectedDates.contains(dateString)
                        val isBooked = bookedDates.contains(dateString)

                        if (isBooked) {
                            android.util.Log.d("CalendarView", "Found booked date: $dateString")
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isBooked -> Color(0xFFBDBDBD)
                                        isSelected -> Color(0xFF2C8B7E)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = !isBooked) {
                                    onDateClicked(dateString)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                fontSize = 14.sp,
                                color = when {
                                    isBooked -> Color(0xFF757575)
                                    isSelected -> Color.White
                                    else -> Color(0xFF2C2C2C)
                                }
                            )
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}