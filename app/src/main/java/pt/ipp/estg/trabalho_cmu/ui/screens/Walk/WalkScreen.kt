package pt.ipp.estg.trabalho_cmu.ui.screens.Walk

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import pt.ipp.estg.trabalho_cmu.R
import android.content.IntentSender
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

/**
 * Screen for active walk tracking.
 *
 * Displays real-time map with route, distance, duration, and animal info.
 * Supports two entry modes:
 * 1. Starting a new walk with a specific animal (animalId provided)
 * 2. Resuming an ongoing walk from notification (animalId is null)
 *
 * When accessed from the notification's "Stop Walk" action, the finish
 * confirmation dialog is automatically displayed.
 *
 * @param navController Navigation controller for screen transitions
 * @param animalId ID of the animal being walked, or null if resuming from notification
 * @param stopRequested If true, automatically shows the finish walk confirmation dialog.
 *                      Used when user taps "Stop Walk" from the notification.
 * @param viewModel Walk view model for managing walk state and tracking
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WalkScreen(
    navController: NavController,
    animalId: String?,
    stopRequested: Boolean = false,
    viewModel: WalkViewModel = viewModel()
) {
    val uiState by viewModel.uiState.observeAsState(WalkUiState.Initial)
    val context = LocalContext.current

    var showFinishDialog by remember { mutableStateOf(false) }

    // Show finish dialog automatically if stop was requested from notification
    LaunchedEffect(stopRequested, uiState) {
        if (stopRequested && uiState is WalkUiState.Tracking) {
            showFinishDialog = true
        }
    }

    // Location permissions state
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            // Notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    )

    // Track if we've already started the walk to avoid duplicate calls
    var walkStarted by remember { mutableStateOf(false) }

    // Track if location services are enabled
    var locationServicesEnabled by remember { mutableStateOf(false) }
    var checkingLocationServices by remember { mutableStateOf(false) }

    // Launcher for location settings resolution
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // Check again after user returns from settings
        locationServicesEnabled = result.resultCode == android.app.Activity.RESULT_OK
    }

    /**
     * Checks if location services (GPS) are enabled and prompts user to enable if not.
     * Uses Google Play Services Location Settings API to show system dialog.
     */
    fun checkLocationServices() {
        checkingLocationServices = true

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).build()

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        val settingsClient = LocationServices.getSettingsClient(context)

        settingsClient.checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                locationServicesEnabled = true
                checkingLocationServices = false
            }
            .addOnFailureListener { exception ->
                checkingLocationServices = false
                if (exception is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingsLauncher.launch(intentSenderRequest)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error
                    }
                }
            }
    }

    // Check location services when permissions are granted
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted && !locationServicesEnabled) {
            checkLocationServices()
        }
    }

    // Start or resume walk when permissions AND location services are ready
    LaunchedEffect(locationPermissions.allPermissionsGranted, locationServicesEnabled, walkStarted) {
        if (locationPermissions.allPermissionsGranted && locationServicesEnabled && !walkStarted) {
            walkStarted = true
            if (animalId != null) {
                // Starting a new walk with specific animal
                viewModel.startWalk(animalId)
            } else {
                // Resuming walk from notification - reconnect to existing service
                viewModel.resumeWalkFromService()
            }
        }
    }

    // Request permissions on first load
    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.walk_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Check permissions first
            if (!locationPermissions.allPermissionsGranted) {
                // Show permission request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.walk_location_permission_title),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.walk_location_permission_message),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { locationPermissions.launchMultiplePermissionRequest() }
                    ) {
                        Text(stringResource(R.string.walk_grant_permission_button))
                    }
                }
            } else if (!locationServicesEnabled) {
                // Show location services request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.walk_location_services_title),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.walk_location_services_message),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { checkLocationServices() },
                        enabled = !checkingLocationServices
                    ) {
                        if (checkingLocationServices) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.walk_enable_location_button))
                        }
                    }
                }
            } else {
                when (val state = uiState) {
                    is WalkUiState.Initial -> {
                        // Show nothing or placeholder
                    }

                    is WalkUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is WalkUiState.Offline -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.walk_offline_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    is WalkUiState.Tracking -> {
                        WalkTrackingContent(
                            state = state,
                            viewModel = viewModel,
                            onFinishClick = { showFinishDialog = true }
                        )
                    }

                    is WalkUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Finish walk confirmation dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text(stringResource(R.string.walk_finish_dialog_title)) },
            text = { Text(stringResource(R.string.walk_finish_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishDialog = false
                        val walkData = viewModel.stopWalk()

                        if (walkData != null) {
                            // Save walk and navigate to summary
                            viewModel.saveWalk(
                                walkData = walkData,
                                onSuccess = { savedWalk ->
                                    // Use the current animal ID from state or walk data
                                    val currentAnimalId = animalId ?: walkData.animalId
                                    navController.navigate("WalkSummary/${savedWalk.id}") {
                                        popUpTo("Walk/$currentAnimalId") { inclusive = true }
                                    }
                                },
                                onError = { error ->
                                    // TODO: Show error to user
                                }
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.walk_finish_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text(stringResource(R.string.walk_finish_cancel))
                }
            }
        )
    }
}

/**
 * Content displayed during active walk tracking.
 *
 * Shows a map with the walked route, animal information card with
 * real-time statistics (duration, distance), and a finish button.
 *
 * @param state Current tracking state containing location and walk data
 * @param viewModel ViewModel for formatting duration and distance values
 * @param onFinishClick Callback when user taps the finish walk button
 */
@Composable
private fun WalkTrackingContent(
    state: WalkUiState.Tracking,
    viewModel: WalkViewModel,
    onFinishClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Map with route
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            WalkMap(
                currentLocation = state.currentLocation,
                routePoints = state.routePoints
            )
        }

        // Walk info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Animal info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = state.animalImageUrl,
                        contentDescription = state.animal.name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = state.animal.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WalkStatItem(
                        label = stringResource(R.string.walk_duration_label),
                        value = viewModel.formatDuration(state.duration)
                    )

                    WalkStatItem(
                        label = stringResource(R.string.walk_distance_label),
                        value = viewModel.formatDistance(state.distance)
                    )

                    WalkStatItem(
                        label = stringResource(R.string.walk_date_label),
                        value = state.date
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Finish button
                Button(
                    onClick = onFinishClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.walk_finish_button))
                }
            }
        }
    }
}

/**
 * Google Maps component for walk tracking.
 *
 * Displays the current location, walked route as a polyline,
 * and markers for start position and current position.
 * Camera automatically follows the user's current location.
 *
 * @param currentLocation Current GPS coordinates, null if not yet available
 * @param routePoints List of GPS coordinates representing the walked route
 */
@Composable
private fun WalkMap(
    currentLocation: LatLng?,
    routePoints: List<LatLng>
) {
    // Default camera position (will update when location is available)
    val defaultPosition = LatLng(38.7223, -9.1393) // Lisbon
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: defaultPosition,
            15f
        )
    }

    // Update camera to follow current location
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 17f),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true
        )
    ) {
        // Draw route as polyline
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = androidx.compose.ui.graphics.Color.Blue,
                width = 10f
            )
        }

        // Marker at start position
        if (routePoints.isNotEmpty()) {
            Marker(
                state = MarkerState(position = routePoints.first()),
                title = "Start"
            )
        }

        // Marker at current position
        currentLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Current Location"
            )
        }
    }
}

/**
 * Individual statistic item displaying a label and value.
 *
 * Used to show walk statistics like duration, distance, and date
 * in a vertically stacked format.
 *
 * @param label Descriptive label for the statistic
 * @param value Formatted value to display
 */
@Composable
private fun WalkStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}