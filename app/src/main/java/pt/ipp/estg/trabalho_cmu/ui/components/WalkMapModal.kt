package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk

/**
 * Full-screen modal dialog displaying an interactive map of a walk route.
 *
 * Shows the complete walk route with start and end markers,
 * along with walk details like animal name, owner, and distance.
 * The map is fully interactive with zoom and pan controls.
 *
 * @param walk Walk data to display
 * @param formattedDistance Formatted distance string for display
 * @param formattedDuration Formatted duration string for display
 * @param onDismiss Callback when user closes the modal
 */
@Composable
fun WalkMapModal(
    walk: Walk,
    formattedDistance: String,
    formattedDuration: String,
    onDismiss: () -> Unit
) {
    val routePoints = parseRoutePoints(walk.routePoints)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with title and close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(
                                R.string.walk_map_title,
                                walk.animalName,
                                walk.userName
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(
                                R.string.walk_map_stats,
                                formattedDistance,
                                formattedDuration
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }

                // Interactive map
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (routePoints.isNotEmpty()) {
                        InteractiveRouteMap(routePoints = routePoints)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.walk_no_route_data),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Google Map showing the walk route.
 *
 * Displays the route polyline with start and end markers.
 * Full zoom and pan controls are enabled.
 *
 * @param routePoints List of GPS coordinates for the route
 */
@Composable
private fun InteractiveRouteMap(
    routePoints: List<LatLng>
) {
    val centerPoint = routePoints.getOrNull(routePoints.size / 2) ?: routePoints.first()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPoint, 15f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    ) {
        // Draw route polyline
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = androidx.compose.ui.graphics.Color.Blue,
                width = 10f
            )
        }

        // Start marker
        Marker(
            state = MarkerState(position = routePoints.first()),
            title = "Start"
        )

        // End marker
        if (routePoints.size > 1) {
            Marker(
                state = MarkerState(position = routePoints.last()),
                title = "End"
            )
        }
    }
}

/**
 * Parse route points from string format to LatLng list.
 *
 * @param routePointsStr String in format "lat,lng;lat,lng;..."
 * @return List of LatLng coordinates
 */
private fun parseRoutePoints(routePointsStr: String): List<LatLng> {
    if (routePointsStr.isEmpty()) return emptyList()

    return try {
        routePointsStr.split(";").mapNotNull { point ->
            val parts = point.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) {
                    LatLng(lat, lng)
                } else null
            } else null
        }
    } catch (e: Exception) {
        emptyList()
    }
}