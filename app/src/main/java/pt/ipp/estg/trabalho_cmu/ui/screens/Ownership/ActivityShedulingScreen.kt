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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityAnimalInfoCard
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityDatesChosen
import pt.ipp.estg.trabalho_cmu.ui.components.CalendarView
import pt.ipp.estg.trabalho_cmu.ui.components.TimeInputFields

@Composable
fun ActivitySchedulingScreen(
    userId: String,
    animalId: String,
    onScheduleSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activityViewModel: ActivityViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var pickupTime by remember { mutableStateOf("09:00") }
    var deliveryTime by remember { mutableStateOf("18:00") }

    // Observar estados
    val uiState by activityViewModel.uiState.observeAsState(ActivityUiState.Initial)
    val animal by activityViewModel.animal.observeAsState()
    val shelter by activityViewModel.shelter.observeAsState()

    // Sync ViewModel times
    LaunchedEffect(pickupTime) { activityViewModel.pickupTime.value = pickupTime }
    LaunchedEffect(deliveryTime) { activityViewModel.deliveryTime.value = deliveryTime }

    LaunchedEffect(animalId) {
        activityViewModel.loadAnimalAndShelter(animalId)
    }

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is ActivityUiState.ActivityScheduled -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Atividade agendada com sucesso!")
                    activityViewModel.resetActivityScheduled()
                    onScheduleSuccess()
                }
            }
            is ActivityUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    val isLoading = uiState is ActivityUiState.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFE8F5F3), Color(0xFFF8FFFE))))
                .padding(paddingValues)
        ) {
            if (animal == null || shelter == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2C8B7E)
                )
            } else {
                ActivitySchedulingContent(
                    animalName = animal!!.name,
                    shelterName = shelter!!.name,
                    shelterAddress = shelter!!.address,
                    shelterContact = shelter!!.phone,
                    imageUrl = animal!!.imageUrls.firstOrNull(),
                    selectedDates = selectedDates,
                    onDatesChanged = { selectedDates = it },
                    pickupTime = pickupTime,
                    onPickupTimeChange = { pickupTime = it },
                    deliveryTime = deliveryTime,
                    onDeliveryTimeChange = { deliveryTime = it },
                    isLoading = isLoading,
                    onScheduleClick = {
                        activityViewModel.scheduleActivity(userId, animalId, selectedDates.toList())
                    }
                )
            }
        }
    }
}

// O Content Composable e Preview mantêm-se iguais (UI pura)
@Composable
private fun ActivitySchedulingContent(
    animalName: String,
    shelterName: String,
    shelterAddress: String,
    shelterContact: String,
    imageUrl: String?,
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
            fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = Color(0xFF2C8B7E), modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ActivityAnimalInfoCard(animalName, shelterName, shelterContact, shelterAddress, imageUrl)
                Spacer(Modifier.height(20.dp))
                CalendarView(selectedDates, onDatesChanged)
                Spacer(Modifier.height(20.dp))
                ActivityDatesChosen(selectedDates)
                Spacer(Modifier.height(16.dp))
                TimeInputFields(pickupTime, deliveryTime, onPickupTimeChange, onDeliveryTimeChange)
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onScheduleClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading && selectedDates.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C8B7E))
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text(stringResource(R.string.visit_schedule_button), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSchedulingContent() {
    ActivitySchedulingContent("Mariana", "Abrigo Felgueiras", "Rua X", "911", "", emptySet(), {}, "09:00", {}, "18:00", {}, false, {})
}