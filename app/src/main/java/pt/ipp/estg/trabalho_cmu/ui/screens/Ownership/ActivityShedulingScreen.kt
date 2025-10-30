package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityAnimalInfoCard
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityDatesChosen
import pt.ipp.estg.trabalho_cmu.ui.components.CalendarView
import pt.ipp.estg.trabalho_cmu.ui.components.TimeInputFields

@Composable
fun ActivitySchedulingScreen(
    userId: String,
    animalId: String,
    onScheduleSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var pickupTime by remember { mutableStateOf("09:00") }
    var deliveryTime by remember { mutableStateOf("18:00") }

    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val activityScheduled by viewModel.activityScheduled.observeAsState(false)

    val animal by viewModel.animal.observeAsState()
    val shelter by viewModel.shelter.observeAsState()

    LaunchedEffect(animalId) {
        viewModel.loadAnimalAndShelter(animalId)
    }

    LaunchedEffect(activityScheduled) {
        if (activityScheduled) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Atividade agendada com sucesso!",
                    duration = SnackbarDuration.Short
                )
                kotlinx.coroutines.delay(500)
                viewModel.resetActivityScheduled()
                onScheduleSuccess()
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                // ✅ Gradiente suave de fundo
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F5F3),  // Verde claro no topo
                            Color(0xFFF8FFFE),  // Branco suave no meio
                            Color(0xFFE8F5F3)   // Verde claro no fundo
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (animal == null || shelter == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2C8B7E)
                )
            } else {
                ActivitySchedulingContent(
                    animalName = animal?.name ?: "",
                    shelterName = shelter?.name ?: "",
                    shelterAddress = shelter?.address ?: "",
                    shelterContact = shelter?.contact ?: "",
                    animalImageRes = R.drawable.cat_image,
                    selectedDates = selectedDates,
                    onDatesChanged = { selectedDates = it },
                    pickupTime = pickupTime,
                    onPickupTimeChange = { pickupTime = it },
                    deliveryTime = deliveryTime,
                    onDeliveryTimeChange = { deliveryTime = it },
                    isLoading = isLoading,
                    onScheduleClick = {
                        if (selectedDates.isNotEmpty()) {
                            val sortedDates = selectedDates.sorted()
                            val activity = Activity(
                                userId = userId,
                                animalId = animalId,
                                pickupDate = sortedDates.first(),
                                pickupTime = pickupTime,
                                deliveryDate = sortedDates.last(),
                                deliveryTime = deliveryTime
                            )
                            viewModel.scheduleActivity(activity)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Por favor, selecione pelo menos uma data",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ActivitySchedulingContent(
    animalName: String,
    shelterName: String,
    shelterAddress: String,
    shelterContact: String,
    animalImageRes: Int,
    selectedDates: Set<String>,
    onDatesChanged: (Set<String>) -> Unit,
    pickupTime: String,
    onPickupTimeChange: (String) -> Unit,
    deliveryTime: String,
    onDeliveryTimeChange: (String) -> Unit,
    isLoading: Boolean,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.visit_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C8B7E),  // ✅ Verde mais forte
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ✅ Card com elevation e sombra mais suave
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White  // ✅ Branco puro no card
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp  // ✅ Mais elevation para destacar
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActivityAnimalInfoCard(
                    animalName = animalName,
                    shelterName = shelterName,
                    shelterContact = shelterContact,
                    shelterAddress = shelterAddress,
                    imageRes = animalImageRes
                )

                Spacer(modifier = Modifier.height(20.dp))

                CalendarView(
                    selectedDates = selectedDates,
                    onDatesChanged = onDatesChanged
                )

                Spacer(modifier = Modifier.height(20.dp))

                ActivityDatesChosen(selectedDates = selectedDates)

                Spacer(modifier = Modifier.height(16.dp))

                TimeInputFields(
                    pickupTime = pickupTime,
                    deliveryTime = deliveryTime,
                    onPickupTimeChange = onPickupTimeChange,
                    onDeliveryTimeChange = onDeliveryTimeChange
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onScheduleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading && selectedDates.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C8B7E),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFB0D4CF)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.visit_schedule_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ActivitySchedulingContentPreview() {
    var selectedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var pickupTime by remember { mutableStateOf("09:00") }
    var deliveryTime by remember { mutableStateOf("18:00") }

    // ✅ Preview com fundo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5F3),
                        Color(0xFFF8FFFE),
                        Color(0xFFE8F5F3)
                    )
                )
            )
    ) {
        ActivitySchedulingContent(
            animalName = "Mariana",
            shelterName = "Abrigo de Felgueiras",
            shelterAddress = "Rua da Saúde, 1234 Santa Marta de Farto",
            shelterContact = "253 000 000",
            animalImageRes = R.drawable.cat_image,
            selectedDates = selectedDates,
            onDatesChanged = { selectedDates = it },
            pickupTime = pickupTime,
            onPickupTimeChange = { pickupTime = it },
            deliveryTime = deliveryTime,
            onDeliveryTimeChange = { deliveryTime = it },
            isLoading = false,
            onScheduleClick = { }
        )
    }
}