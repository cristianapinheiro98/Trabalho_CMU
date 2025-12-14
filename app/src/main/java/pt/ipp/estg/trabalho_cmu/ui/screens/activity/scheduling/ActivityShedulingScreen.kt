package pt.ipp.estg.trabalho_cmu.ui.screens.activity.scheduling

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityAnimalInfoCard
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityDatesChosen
import pt.ipp.estg.trabalho_cmu.ui.components.ActivitySuccessDialog
import pt.ipp.estg.trabalho_cmu.ui.components.CalendarView
import pt.ipp.estg.trabalho_cmu.ui.components.TimeInputFields

/**
 * Main screen for scheduling a new activity (visit/walk) with a specific animal.
 *
 * This stateful composable manages the [ActivitySchedulingViewModel], handles data loading,
 * observes UI states (Loading, Success, Error, Offline), and displays the success dialog.
 *
 * @param userId The unique identifier of the current user.
 * @param animalId The unique identifier of the animal to be scheduled.
 * @param onScheduleSuccess Callback triggered when the scheduling is successfully completed.
 * @param modifier Modifier for styling and layout customization.
 */
@Composable
fun ActivitySchedulingScreen(
    userId: String,
    animalId: String,
    onScheduleSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ActivitySchedulingViewModel = viewModel()

    val snackbarHostState = remember { SnackbarHostState() }

    // State for showing success dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogData by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // Observe UIState
    val uiState by viewModel.uiState.observeAsState(ActivitySchedulingUiState.Initial)

    // Obtain strings outside of the launched effect
    val offlineMessage = stringResource(R.string.error_offline_scheduling)

    // Load data
    LaunchedEffect(animalId) {
        viewModel.loadSchedulingData(animalId, userId)
    }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ActivitySchedulingUiState.SchedulingSuccess -> {
                successDialogData = Triple(state.animalName, state.startDate, state.endDate)
                showSuccessDialog = true
            }
            is ActivitySchedulingUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is ActivitySchedulingUiState.Offline -> {
                snackbarHostState.showSnackbar(offlineMessage)
            }
            else -> {}
        }
    }

    if (showSuccessDialog && successDialogData != null) {
        ActivitySuccessDialog(
            animalName = successDialogData!!.first,
            startDate = successDialogData!!.second,
            endDate = successDialogData!!.third,
            onDismiss = {
                showSuccessDialog = false
                onScheduleSuccess()
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFE8F5F3), Color(0xFFF8FFFE))))
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ActivitySchedulingUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2C8B7E)
                    )
                }
                is ActivitySchedulingUiState.Success -> {
                    ActivitySchedulingContent(
                        animal = state.animal,
                        shelter = state.shelter,
                        selectedDates = state.selectedDates,
                        onDateClicked = { viewModel.onDateClicked(it) },
                        pickupTime = state.pickupTime,
                        onPickupTimeChange = { viewModel.onPickupTimeChanged(it) },
                        deliveryTime = state.deliveryTime,
                        onDeliveryTimeChange = { viewModel.onDeliveryTimeChanged(it) },
                        validationError = state.validationError,
                        onScheduleClick = { viewModel.scheduleActivity(userId) },
                        onClearError = { viewModel.clearError() },
                        bookedDates = state.bookedDates
                    )
                }
                else -> {}
            }
        }
    }
}

/**
 * Stateless content composable representing the scheduling form.
 *
 * Displays the animal's details, a calendar for date selection, time input fields,
 * shelter operating hours, and validation errors.
 *
 * @param animal The animal entity to display details for.
 * @param shelter The shelter entity to display address and hours.
 * @param selectedDates A set of dates currently selected by the user.
 * @param onDateClicked Callback triggered when a date on the calendar is clicked.
 * @param pickupTime Current value of the pickup time input.
 * @param onPickupTimeChange Callback triggered when pickup time changes.
 * @param deliveryTime Current value of the delivery time input.
 * @param onDeliveryTimeChange Callback triggered when delivery time changes.
 * @param validationError Current validation error state (if any).
 * @param onScheduleClick Callback triggered when the confirm button is pressed.
 * @param onClearError Callback triggered to clear displayed errors.
 * @param bookedDates List of dates that are already booked and unavailable.
 * @param modifier Modifier for styling.
 */
@Composable
private fun ActivitySchedulingContent(
    animal: Animal,
    shelter: Shelter,
    selectedDates: Set<String>,
    onDateClicked: (String) -> Unit,
    pickupTime: String,
    onPickupTimeChange: (String) -> Unit,
    deliveryTime: String,
    onDeliveryTimeChange: (String) -> Unit,
    validationError: ValidationError?,
    onScheduleClick: () -> Unit,
    onClearError: () -> Unit,
    bookedDates: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Show validation errors
    LaunchedEffect(validationError) {
        validationError?.let {
            // Handle validation errors if needed
        }
    }

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
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActivityAnimalInfoCard(
                    animal.name,
                    shelter.name,
                    shelter.phone,
                    shelter.address,
                    animal.imageUrls.firstOrNull()
                )

                Spacer(Modifier.height(20.dp))

                // Show shelter hours if available
                if (shelter.openingTime != null && shelter.closingTime != null) {
                    Text(
                        text = stringResource(R.string.shelter_hours_info, shelter.openingTime!!, shelter.closingTime!!),
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                CalendarView(
                    selectedDates = selectedDates,
                    onDateClicked = onDateClicked,
                    bookedDates = bookedDates
                )

                Spacer(Modifier.height(20.dp))

                ActivityDatesChosen(selectedDates)

                Spacer(Modifier.height(16.dp))

                TimeInputFields(pickupTime, deliveryTime, onPickupTimeChange, onDeliveryTimeChange)

                Spacer(Modifier.height(20.dp))

                // Show validation error
                validationError?.let { error ->
                    Text(
                        text = when (error) {
                            ValidationError.TimeOutsideOpeningHours -> stringResource(R.string.error_time_outside_hours)
                            ValidationError.DateConflict -> stringResource(R.string.error_date_conflict)
                            ValidationError.LessThan24Hours -> stringResource(R.string.error_less_than_24h)
                            ValidationError.ActiveActivityExists -> stringResource(R.string.error_active_activity_exists)
                            ValidationError.NoDateSelected -> stringResource(R.string.error_no_date_selected)
                        },
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = onScheduleClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = selectedDates.isNotEmpty() && validationError == null,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C8B7E))
                ) {
                    Text(
                        stringResource(R.string.visit_schedule_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}