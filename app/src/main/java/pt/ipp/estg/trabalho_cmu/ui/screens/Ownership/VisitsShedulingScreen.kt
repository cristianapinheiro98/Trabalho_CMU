package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun VisitSchedulingScreen(
    animalName: String = "Mariana",
    animalBreed: String = "Golden Retriever",
    animalLocation: String = "Abrigo de Felgueiras",
    shelterAddress: String = "Rua da Saúde, 1234 Santa Marta de Farto",
    onScheduleVisit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var pickupTime by remember { mutableStateOf("9:00") }
    var deliveryTime by remember { mutableStateOf("18:00") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFB8D4D0)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título
                    Text(
                        text = stringResource(R.string.visit_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Informação do animal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagem do animal
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cat_image),  // Certifique-se de que o nome da imagem esteja correto
                                contentDescription = "Imagem do Animal",
                                modifier = Modifier.size(80.dp)  // Ajuste o tamanho conforme necessário
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = animalName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C)
                            )
                            Text(
                                text = animalBreed,
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                            Text(
                                text = animalLocation,
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                            Text(
                                text = shelterAddress,
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calendário
                    CalendarView(
                        selectedDates = selectedDates,
                        onDatesChanged = { selectedDates = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Datas selecionadas
                    if (selectedDates.isNotEmpty()) {
                        val sortedDates = selectedDates.sorted()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "${stringResource(R.string.visit_start_label)} ${sortedDates.first()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C)
                            )
                            Text(
                                text = "${stringResource(R.string.visit_end_label)} ${sortedDates.last()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C)
                            )
                        }
                    }

                    // Campos de hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = pickupTime,
                            onValueChange = { newText ->
                                pickupTime = formatTimeInput(newText)
                            },
                            label = { Text(stringResource(R.string.visit_pickup_time)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.width(120.dp)
                        )
                        OutlinedTextField(
                            value = deliveryTime,
                            onValueChange = { newText ->
                                deliveryTime = formatTimeInput(newText) // Usa a função formatadora
                            },
                            label = { Text(stringResource(R.string.visit_delivery_time)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.width(120.dp)
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))


                    // Botão de agendar
                    Button(
                        onClick = onScheduleVisit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2C8B7E),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.visit_schedule_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CalendarView(
    selectedDates: Set<String>,
    onDatesChanged: (Set<String>) -> Unit
) {
    val currentCalendar = remember { Calendar.getInstance() }
    val year = currentCalendar.get(Calendar.YEAR)
    val month = currentCalendar.get(Calendar.MONTH)

    // Primeiro dia do mês
    val firstDayCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val daysInMonth = firstDayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = firstDayCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Domingo

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
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
                                    val newDates = if (isSelected) {
                                        selectedDates - dateString
                                    } else {
                                        selectedDates + dateString
                                    }
                                    onDatesChanged(newDates)
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

// Função reutilizável para formatar o horário
fun formatTimeInput(newText: String): String {
    if (newText.isEmpty()) return newText // Não aplica formatação se estiver vazio

    var formattedText = newText.replace(Regex("[^0-9:]"), "") // Remove qualquer caractere que não seja número ou ":"
    if (formattedText.length in 3..4 && formattedText[2] != ':') {
        // Se o comprimento for 3 ou 4, insere o ":" no meio
        formattedText = formattedText.substring(0, 2) + ":" + formattedText.substring(2)
    }
    return if (formattedText.length <= 5) formattedText else formattedText.substring(0, 5) // Limita a 5 caracteres
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VisitSchedulingScreenPreview() {
    VisitSchedulingScreen()
}

/*Depois de ter room + firebase ver no C as adaptações que tenho que fazer*/