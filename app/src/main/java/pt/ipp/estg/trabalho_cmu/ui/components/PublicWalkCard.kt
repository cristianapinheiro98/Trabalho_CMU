package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk

/**
 * Card displaying a public walk in the SocialTails community feed.
 *
 * Shows the animal image, walk description with owner and animal names,
 * distance walked, and a mini interactive map preview of the route.
 *
 * @param walk Walk data to display
 * @param relativeDate Formatted relative date (e.g., "Today", "Yesterday")
 * @param formattedDistance Formatted distance string
 * @param onMapClick Callback when user taps the map to expand it
 * @param modifier Optional modifier
 */
@Composable
fun PublicWalkCard(
    walk: Walk,
    relativeDate: String,
    formattedDistance: String,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routePoints = parseRoutePoints(walk.routePoints)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animal image (left)
            AsyncImage(
                model = walk.animalImageUrl,
                contentDescription = walk.animalName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Walk description (center)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Relative date
                Text(
                    text = relativeDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Walk description text
                Text(
                    text = stringResource(
                        R.string.public_walk_description,
                        walk.userName,
                        walk.animalName,
                        formattedDistance
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Mini map (right)
            Card(
                modifier = Modifier
                    .size(72.dp)
                    .clickable { onMapClick() },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (routePoints.isNotEmpty()) {
                    MiniMapPreview(routePoints = routePoints)
                }
            }
        }
    }
}

/**
 * Mini map preview showing the walk route.
 *
 * Displays a non-interactive map thumbnail with the route polyline.
 * User interaction is handled by the parent clickable modifier.
 *
 * @param routePoints List of GPS coordinates for the route
 */
@Composable
private fun MiniMapPreview(
    routePoints: List<LatLng>
) {
    val centerPoint = routePoints.getOrNull(routePoints.size / 2) ?: routePoints.first()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPoint, 14f)
    }

    GoogleMap(
        modifier = Modifier.size(72.dp),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false,
            scrollGesturesEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    ) {
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = androidx.compose.ui.graphics.Color.Blue,
                width = 8f
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