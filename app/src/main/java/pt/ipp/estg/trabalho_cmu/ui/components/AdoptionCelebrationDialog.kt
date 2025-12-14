package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import pt.ipp.estg.trabalho_cmu.R

/**
 * Celebration dialog shown when an adoption request is approved.
 * Features animated floating hearts overlaying the animal's image.
 *
 * @param userName User's name for personalized greeting
 * @param animalName Animal's name
 * @param animalImageUrl URL of the animal's first image
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun AdoptionCelebrationDialog(
    userName: String,
    animalName: String,
    animalImageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFB2DFDB)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Congratulations text
                Text(
                    text = stringResource(R.string.celebration_thanks, userName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF004D40) // Dark teal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.celebration_eager_to_meet),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF00695C) // Medium teal
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Animal image with floating hearts
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animal image
                    AsyncImage(
                        model = animalImageUrl,
                        contentDescription = animalName,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Animated floating hearts
                    FloatingHearts()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Go to main menu button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B) // Teal button
                    )
                ) {
                    Text(
                        text = stringResource(R.string.celebration_go_to_menu),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Animated floating hearts component that creates the balloon-like effect
 * Hearts float and pulse to create a celebratory atmosphere
 */
@Composable
private fun FloatingHearts() {
    // Heart 1 - Left side, larger
    AnimatedHeart(
        offsetX = -70.dp.value,
        offsetY = 60.dp.value,
        size = 80f,
        delay = 0,
        color = Color(0xFFE57373) // Light red
    )

    // Heart 2 - Right side, slightly smaller
    AnimatedHeart(
        offsetX = 50.dp.value,
        offsetY = 40.dp.value,
        size = 70f,
        delay = 150,
        color = Color(0xFFEF5350) // Red
    )

    // Heart 3 - Small accent heart (optional)
    AnimatedHeart(
        offsetX = -20.dp.value,
        offsetY = 80.dp.value,
        size = 40f,
        delay = 300,
        color = Color(0xFFE57373).copy(alpha = 0.7f)
    )
}

/**
 * Single animated heart with floating and pulsing effect
 *
 * @param offsetX Horizontal offset from center
 * @param offsetY Vertical offset from center
 * @param size Base size of the heart
 * @param delay Animation start delay in milliseconds
 * @param color Heart color
 */
@Composable
private fun AnimatedHeart(
    offsetX: Float,
    offsetY: Float,
    size: Float,
    delay: Int,
    color: Color
) {
    // Infinite transition for continuous animation
    val infiniteTransition = rememberInfiniteTransition(label = "heart")

    // Floating animation (up and down)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Pulse animation (scale)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Slight rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Entry animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }

    val entryScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entry"
    )

    Box(
        modifier = Modifier
            .offset(
                x = offsetX.dp,
                y = (offsetY - floatOffset).dp
            )
            .scale(scale * entryScale)
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        Text(
            text = "❤️",
            fontSize = size.sp,
            color = color
        )
    }
}