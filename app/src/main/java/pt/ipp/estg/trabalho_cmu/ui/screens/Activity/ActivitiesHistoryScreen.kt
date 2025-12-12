package pt.ipp.estg.trabalho_cmu.ui.screens.Activity

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityAnimalInfoCard
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityDateTimeCard
import pt.ipp.estg.trabalho_cmu.ui.components.MapLocationButton
import pt.ipp.estg.trabalho_cmu.utils.openGoogleMaps
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivitiesHistoryScreen(
    userId: String,
    modifier: Modifier = Modifier
) {
    val viewModel: ActivitiesHistoryViewModel = viewModel()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Observe UIState
    val uiState by viewModel.uiState.observeAsState(ActivitiesHistoryUiState.Initial)

    // Load data starting screen
    LaunchedEffect(Unit) {
        viewModel.loadActivities(userId)
    }

    // Handle state messages
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ActivitiesHistoryUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = uiState) {
                is ActivitiesHistoryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2C8B7E)
                    )
                }

                is ActivitiesHistoryUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_activities_scheduled),
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }

                is ActivitiesHistoryUiState.OnlineSuccess -> {
                    ActivitiesHistoryContent(
                        activities = state.activities,
                        isOffline = false,
                        onDeleteActivity = { activity ->
                            viewModel.deleteActivity(activity.activity.id, userId)
                        }
                    )
                }

                is ActivitiesHistoryUiState.OfflineSuccess -> {
                    Column {
                        // Offline warning banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFF3CD)
                        ) {
                            Text(
                                text = stringResource(R.string.offline_warning),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF856404)
                            )
                        }

                        ActivitiesHistoryContent(
                            activities = state.activities,
                            isOffline = true,
                            onDeleteActivity = { activity ->
                                viewModel.deleteActivity(activity.activity.id, userId)
                            }
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ActivitiesHistoryContent(
    activities: List<ActivityWithDetails>,
    isOffline: Boolean,
    onDeleteActivity: (ActivityWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val today = Date()

    val (ongoing, upcoming, past) = categorizeActivities(activities, today)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.activities_history_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2C),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Ongoing Activities
        if (ongoing.isNotEmpty()) {
            CategoryHeader(stringResource(R.string.ongoing_activities))
            ongoing.forEach { item ->
                ActivityCard(
                    item = item,
                    context = context,
                    onDelete = { onDeleteActivity(item) }
                )
            }
        }

        // Upcoming Activities
        if (upcoming.isNotEmpty()) {
            CategoryHeader(stringResource(R.string.upcoming_activities))
            upcoming.forEach { item ->
                ActivityCard(
                    item = item,
                    context = context,
                    onDelete = { onDeleteActivity(item) }
                )
            }
        }

        // Past Activities
        if (past.isNotEmpty()) {
            CategoryHeader(stringResource(R.string.past_activities))
            past.forEach { item ->
                ActivityCard(
                    item = item,
                    context = context,
                    onDelete = null // Não permite cancelar atividades passadas
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF2C8B7E),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun ActivityCard(
    item: ActivityWithDetails,
    context: android.content.Context,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB8D4D0)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActivityAnimalInfoCard(
                animalName = item.animal.name,
                shelterName = item.shelter.name,
                shelterContact = item.shelter.phone,
                shelterAddress = item.shelter.address,
                imageUrl = item.animal.imageUrls.firstOrNull()
            )

            Spacer(Modifier.height(12.dp))

            MapLocationButton(
                onClick = { openGoogleMaps(context, item.shelter.address) }
            )

            Spacer(Modifier.height(12.dp))

            ActivityDateTimeCard(
                pickupDate = item.activity.pickupDate,
                pickupTime = item.activity.pickupTime,
                deliveryDate = item.activity.deliveryDate,
                deliveryTime = item.activity.deliveryTime
            )

            // Só mostra botão de cancelar para atividades futuras
            onDelete?.let {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel_visit_button))
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_warning_title)) },
            text = { Text(stringResource(R.string.delete_activity_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete?.invoke()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

/**
 * Categorizes activities into ongoing, upcoming, and past.
 */
private fun categorizeActivities(
    activities: List<ActivityWithDetails>,
    today: Date
): Triple<List<ActivityWithDetails>, List<ActivityWithDetails>, List<ActivityWithDetails>> {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val ongoing = mutableListOf<ActivityWithDetails>()
    val upcoming = mutableListOf<ActivityWithDetails>()
    val past = mutableListOf<ActivityWithDetails>()

    activities.forEach { item ->
        val pickupDate = sdf.parse(item.activity.pickupDate) ?: return@forEach
        val deliveryDate = sdf.parse(item.activity.deliveryDate) ?: return@forEach

        when {
            // Ongoing: today is between pickup and delivery
            !today.before(pickupDate) && !today.after(deliveryDate) -> ongoing.add(item)
            // Upcoming: pickup is in the future
            today.before(pickupDate) -> upcoming.add(item)
            // Past: delivery is in the past
            else -> past.add(item)
        }
    }

    return Triple(ongoing, upcoming, past)
}
