package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk
import pt.ipp.estg.trabalho_cmu.ui.theme.BronzeTrophy
import pt.ipp.estg.trabalho_cmu.ui.theme.GrayTrophy
import pt.ipp.estg.trabalho_cmu.ui.theme.YellowTrophy

/**
 * Podium section displaying top 3 walks with trophies.
 *
 * Shows a horizontal podium with gold in the center (tallest),
 * silver on the left (medium height), and bronze on the right (shortest).
 * Each podium place displays the trophy, animal image, animal name,
 * and owner name.
 *
 * @param title Section title (e.g., "All Time" or "December 2025")
 * @param walks List of top walks (up to 3)
 * @param formatDuration Function to format duration for display
 * @param modifier Optional modifier
 */
@Composable
fun PodiumSection(
    title: String,
    walks: List<Walk>,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Podium layout: Silver (2nd) | Gold (1st) | Bronze (3rd)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place - Silver (left, medium height)
                PodiumPlace(
                    walk = walks.getOrNull(1),
                    position = 2,
                    podiumHeight = 135.dp.value.toInt(),
                    trophyIcon = R.drawable.ic_trophy_silver,
                    podiumColor = GrayTrophy,
                    formatDuration = formatDuration,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 1st Place - Gold (center, tallest)
                PodiumPlace(
                    walk = walks.getOrNull(0),
                    position = 1,
                    podiumHeight = 145.dp.value.toInt(),
                    trophyIcon = R.drawable.ic_trophy_gold,
                    podiumColor = YellowTrophy,
                    formatDuration = formatDuration,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 3rd Place - Bronze (right, shortest)
                PodiumPlace(
                    walk = walks.getOrNull(2),
                    position = 3,
                    podiumHeight = 115.dp.value.toInt(),
                    trophyIcon = R.drawable.ic_trophy_bronze,
                    podiumColor = BronzeTrophy,
                    formatDuration = formatDuration,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual podium place with trophy and walk information.
 *
 * @param walk Walk data or null if place is empty
 * @param position Podium position (1, 2, or 3)
 * @param podiumHeight Height of the podium block in dp
 * @param trophyIcon Resource ID for the trophy icon
 * @param podiumColor Background color for the podium block
 * @param formatDuration Function to format duration for display
 * @param modifier Optional modifier
 */
@Composable
private fun PodiumPlace(
    walk: Walk?,
    position: Int,
    podiumHeight: Int,
    trophyIcon: Int,
    podiumColor: Color,
    formatDuration: (Long) -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Trophy icon
        Image(
            painter = painterResource(id = trophyIcon),
            contentDescription = "Position $position trophy",
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Podium block with walk info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(podiumColor.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            if (walk != null) {
                // Walk information
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animal image
                    AsyncImage(
                        model = walk.animalImageUrl,
                        contentDescription = walk.animalName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Animal name
                    Text(
                        text = walk.animalName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    // Owner name with "do/da" prefix
                    Text(
                        text = "de ${walk.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Duration
                    Text(
                        text = formatDuration(walk.duration),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Empty place
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${position}º",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = stringResource(R.string.podium_empty),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}