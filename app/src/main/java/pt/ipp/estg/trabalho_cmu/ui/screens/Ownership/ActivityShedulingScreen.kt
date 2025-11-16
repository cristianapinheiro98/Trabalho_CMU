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
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityAnimalInfoCard
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityDatesChosen
import pt.ipp.estg.trabalho_cmu.ui.components.CalendarView
import pt.ipp.estg.trabalho_cmu.ui.components.TimeInputFields

@Composable
fun ActivitySchedulingScreen(
    userId: Int,
    animalId: Int,
    onScheduleSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ActivityViewModel = viewModel()

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

    // Load data
    LaunchedEffect(animalId) {
        viewModel.loadAnimalAndShelter(animalId)
    }

    // Navigate when success
    LaunchedEffect(activityScheduled) {
        if (activityScheduled) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Atividade agendada com sucesso!")
                kotlinx.coroutines.delay(500)
                viewModel.resetActivityScheduled()
                onScheduleSuccess()
            }
        }
    }

    // Show error
    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFE8F5F3),
                            Color(0xFFF8FFFE),
                            Color(0xFFE8F5F3)
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
                        if (selectedDates.isNotEmpty()) {
                            val sorted = selectedDates.sorted()
                            val activity = Activity(
                                userId = userId,
                                animalId = animalId,
                                pickupDate = sorted.first(),
                                pickupTime = pickupTime,
                                deliveryDate = sorted.last(),
                                deliveryTime = deliveryTime
                            )
                            viewModel.scheduleActivity(activity)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Selecione pelo menos uma data")
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C8B7E),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    imageUrl = imageUrl
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
                        contentColor = Color.White
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

@Preview(showBackground = true)
@Composable
private fun PreviewSchedulingContent() {
    ActivitySchedulingContent(
        animalName = "Mariana",
        shelterName = "Abrigo Felgueiras",
        shelterAddress = "Rua da Saúde 123",
        shelterContact = "253 000 000",
        imageUrl = "",
        selectedDates = emptySet(),
        onDatesChanged = {},
        pickupTime = "09:00",
        onPickupTimeChange = {},
        deliveryTime = "18:00",
        onDeliveryTimeChange = {},
        isLoading = false,
        onScheduleClick = {}
    )
}
