package pt.ipp.estg.trabalho_cmu.ui.screens.Veterinarians

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import pt.ipp.estg.trabalho_cmu.ui.components.VeterinarianCard

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VeterinariansScreen(
    viewModel: VeterinariansViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Location permission
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Side Effect: manage permissions and location
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            viewModel.onPermissionGranted()

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
        } else {
            viewModel.onPermissionDenied()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Centros Veterinários perto de ti",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Render UI based on state
        when (val state = uiState) {
            is VeterinariansUiState.LoadingLocation -> {
                LoadingState()
            }

            is VeterinariansUiState.NoPermission -> {
                LocationPermissionRequest(
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() }
                )
            }

            is VeterinariansUiState.GpsDisabledNoCache -> {
                GpsDisabledNoCache(
                    onOpenSettings = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                )
            }

            is VeterinariansUiState.GpsDisabledWithCache -> {
                Column {
                    GpsDisabledWithCacheMessage()
                    Spacer(modifier = Modifier.height(16.dp))
                    VeterinariansList(
                        veterinarians = state.veterinarians,
                        context = context
                    )
                }
            }

            is VeterinariansUiState.EmptyList -> {
                EmptyVeterinariansState(
                    onRefresh = {
                        // Precisa pedir localização novamente
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    viewModel.refreshVeterinarians(it.latitude, it.longitude)
                                }
                            }
                        } catch (e: SecurityException) {
                            // Ignora
                        }
                    }
                )
            }

            is VeterinariansUiState.Success -> {
                VeterinariansList(
                    veterinarians = state.veterinarians,
                    context = context
                )
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun VeterinariansList(
    veterinarians: List<pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian>,
    context: android.content.Context
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(veterinarians) { vet ->
            VeterinarianCard(
                veterinarian = vet,
                onPhoneClick = { phone ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    context.startActivity(intent)
                },
                onMapClick = {
                    openGoogleMaps(context, vet.latitude, vet.longitude, vet.name)
                }
            )
        }
    }
}

@Composable
fun LocationPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Precisamos da tua localização",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Para mostrar veterinários perto de ti",
            fontSize = 14.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Permitir Localização")
        }
    }
}

@Composable
fun GpsDisabledNoCache(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📍 GPS Desligado",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Para encontrar veterinários perto de ti,\nliga o GPS nas definições",
            fontSize = 14.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Abrir Definições")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Centros veterinários indisponíveis",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun GpsDisabledWithCacheMessage() {
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
                text = "GPS desligado. A mostrar resultados anteriores.",
                fontSize = 13.sp,
                color = Color(0xFF856404)
            )
        }
    }
}

@Composable
fun EmptyVeterinariansState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhum veterinário encontrado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tenta alargar a área de pesquisa",
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Tentar novamente")
        }
    }
}

private fun openGoogleMaps(context: android.content.Context, lat: Double, lng: Double, name: String) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($name)")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
            "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        ))
        context.startActivity(webIntent)
    }
}