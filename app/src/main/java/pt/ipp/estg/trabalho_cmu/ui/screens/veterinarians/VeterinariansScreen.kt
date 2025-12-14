package pt.ipp.estg.trabalho_cmu.ui.screens.veterinarians

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian
import pt.ipp.estg.trabalho_cmu.ui.components.VeterinarianCard

/**
 * Main screen displaying nearby veterinary centers.
 *
 * Handles location permissions, GPS state, and displays a list of veterinarians
 * based on the user's current location.
 *
 * @param viewModel ViewModel managing the screen state.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VeterinariansScreen(
    windowSize: WindowWidthSizeClass,
    viewModel: VeterinariansViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.observeAsState(VeterinariansUiState.LoadingLocation)

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Handle permission state changes
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.onPermissionGranted()
            requestLocation(context, viewModel)
        } else {
            viewModel.onPermissionDenied()
        }
    }

    VeterinariansScreenContent(
        windowSize=windowSize,
        uiState = uiState,
        onRequestPermission = { locationPermissionState.launchPermissionRequest() },
        onOpenSettings = {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        },
        onRefresh = { requestLocation(context, viewModel) },
        onPhoneClick = { phone ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            context.startActivity(intent)
        },
        onMapClick = { vet ->
            openGoogleMaps(context, vet.latitude, vet.longitude, vet.name)
        }
    )
}

/**
 * Stateless content composable for the Veterinarians screen.
 *
 * @param uiState Current UI state.
 * @param onRequestPermission Callback to request location permission.
 * @param onOpenSettings Callback to open device location settings.
 * @param onRefresh Callback to refresh the veterinarians list.
 * @param onPhoneClick Callback when phone button is clicked.
 * @param onMapClick Callback when map button is clicked.
 */
@Composable
private fun VeterinariansScreenContent(
    windowSize: WindowWidthSizeClass,
    uiState: VeterinariansUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onPhoneClick: (String) -> Unit,
    onMapClick: (Veterinarian) -> Unit
) {
    val isTablet =
        windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded
    val screenPadding = if (isTablet) 24.dp else 16.dp
    val maxWidth = if (isTablet) 1200.dp else 600.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(screenPadding)
                .widthIn(max = maxWidth)
        ) {
            Text(
                text = stringResource(R.string.veterinarians_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is VeterinariansUiState.LoadingLocation -> {
                    LoadingState()
                }

                is VeterinariansUiState.NoPermission -> {
                    LocationPermissionRequest(onRequestPermission = onRequestPermission)
                }

                is VeterinariansUiState.GpsDisabledNoCache -> {
                    GpsDisabledNoCache(onOpenSettings = onOpenSettings)
                }

                is VeterinariansUiState.GpsDisabledWithCache -> {
                    Column {
                        GpsDisabledWithCacheMessage()
                        Spacer(modifier = Modifier.height(16.dp))
                        VeterinariansList(
                            windowSize = windowSize,
                            veterinarians = uiState.veterinarians,
                            onPhoneClick = onPhoneClick,
                            onMapClick = onMapClick
                        )
                    }
                }

                is VeterinariansUiState.EmptyList -> {
                    EmptyVeterinariansState(onRefresh = onRefresh)
                }

                is VeterinariansUiState.Success -> {
                    VeterinariansList(
                        windowSize = windowSize,
                        veterinarians = uiState.veterinarians,
                        onPhoneClick = onPhoneClick,
                        onMapClick = onMapClick
                    )
                }
            }
        }
    }
}

/**
 * Loading indicator displayed while obtaining location.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF4CAF50))
    }
}

/**
 * List of veterinarian cards.
 *
 * @param veterinarians List of veterinarians to display.
 * @param onPhoneClick Callback when phone button is clicked.
 * @param onMapClick Callback when map button is clicked.
 */
@Composable
private fun VeterinariansList(
    windowSize: WindowWidthSizeClass,
    veterinarians: List<Veterinarian>,
    onPhoneClick: (String) -> Unit,
    onMapClick: (Veterinarian) -> Unit
) {
    val columns = when (windowSize) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 2
        else -> 1
    }
    val spacing = if (columns == 1) 16.dp else 12.dp
    val gridPadding = if (columns == 1) 0.dp else 4.dp

    if(columns==1) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(veterinarians, key = { it.placeId }) { vet ->
                VeterinarianCard(
                    veterinarian = vet,
                    onPhoneClick = { onPhoneClick(vet.phone ?: "") },
                    onMapClick = { onMapClick(vet) }
                )
            }
        }
    }else{
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(gridPadding)
        ) {
            items(veterinarians, key = { it.placeId }) { vet ->
                VeterinarianCard(
                    veterinarian = vet,
                    onPhoneClick = { onPhoneClick(vet.phone ?: "") },
                    onMapClick = { onMapClick(vet) }
                )
            }
        }

    }
}

/**
 * UI for requesting location permission.
 *
 * @param onRequestPermission Callback to trigger permission request.
 */
@Composable
private fun LocationPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.location_permission_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.location_permission_message),
            fontSize = 14.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(stringResource(R.string.allow_location_button))
        }
    }
}

/**
 * UI displayed when GPS is disabled and no cached data is available.
 *
 * @param onOpenSettings Callback to open device settings.
 */
@Composable
private fun GpsDisabledNoCache(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.gps_disabled_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.gps_disabled_message),
            fontSize = 14.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(stringResource(R.string.open_settings_button))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.veterinarians_unavailable),
            fontSize = 14.sp,
            color = Color(0xFF999999),
            fontStyle = FontStyle.Italic
        )
    }
}

/**
 * Warning message displayed when GPS is disabled but cached data is shown.
 */
@Composable
private fun GpsDisabledWithCacheMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚠️", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.gps_disabled_cache_warning),
                fontSize = 13.sp,
                color = Color(0xFF856404)
            )
        }
    }
}

/**
 * UI displayed when no veterinarians are found nearby.
 *
 * @param onRefresh Callback to retry the search.
 */
@Composable
private fun EmptyVeterinariansState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_veterinarians_found),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.try_expanding_search),
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(stringResource(R.string.try_again_button))
        }
    }
}

/**
 * Requests the user's current location and notifies the ViewModel.
 *
 * @param context Android context.
 * @param viewModel ViewModel to notify with location results.
 */
private fun requestLocation(
    context: android.content.Context,
    viewModel: VeterinariansViewModel
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.onLocationReceived(location.latitude, location.longitude)
            } else {
                viewModel.onGpsDisabled()
            }
        }
    } catch (e: SecurityException) {
        viewModel.onGpsDisabled()
    }
}

/**
 * Opens Google Maps with the specified location.
 *
 * @param context Android context.
 * @param lat Latitude of the location.
 * @param lng Longitude of the location.
 * @param name Name to display on the map.
 */
private fun openGoogleMaps(
    context: android.content.Context,
    lat: Double,
    lng: Double,
    name: String
) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($name)")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        )
        context.startActivity(webIntent)
    }
}