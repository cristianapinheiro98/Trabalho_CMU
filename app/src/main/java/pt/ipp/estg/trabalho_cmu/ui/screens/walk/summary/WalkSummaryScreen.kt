package pt.ipp.estg.trabalho_cmu.ui.screens.walk.summary

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import pt.ipp.estg.trabalho_cmu.R
import java.util.concurrent.TimeUnit
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

/**
 * Screen for walk summary after completion
 * Displays walk route, stats, medal, and action buttons
 *
 * @param navController Navigation controller
 * @param walkId ID of the completed walk
 * @param viewModel Walk summary view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkSummaryScreen(
    navController: NavController,
    walkId: String,
    viewModel: WalkSummaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.observeAsState(WalkSummaryUiState.Initial)

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var celebrationShown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Load walk summary when screen loads
    LaunchedEffect(walkId) {
        viewModel.loadWalkSummaryById(walkId)
    }

    // Show celebration when medal is earned (only once)
    LaunchedEffect(uiState) {
        if (uiState is WalkSummaryUiState.Success && !celebrationShown) {
            val state = uiState as WalkSummaryUiState.Success
            if (state.medalEmoji != null) {
                showCelebration = true
                celebrationShown = true
            }
        }
    }

    // Handle state changes
    // Handle state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is WalkSummaryUiState.SavedToHistory -> {
                navController.navigate("WalkHistory") {
                    popUpTo("UserHome") { inclusive = false }
                }
            }
            is WalkSummaryUiState.SharedToSocialTails -> {
                // Show toast and navigate to SocialTails Community
                Toast.makeText(
                    context,
                    context.getString(R.string.walk_shared_success),
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate("SocialTailsCommunity") {
                    popUpTo("UserHome") { inclusive = false }
                }
            }
            is WalkSummaryUiState.Discarded -> {
                navController.navigate("UserHome") {
                    popUpTo("UserHome") { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.walk_summary_title)) },
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
                when (val state = uiState) {
                    is WalkSummaryUiState.Initial,
                    is WalkSummaryUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is WalkSummaryUiState.Success -> {
                        WalkSummaryContent(
                            state = state,
                            onSaveToHistory = { viewModel.saveToHistory() },
                            onDiscard = { showDiscardDialog = true },
                            onShareToCommunity = { viewModel.shareToSocialTails(walkId) }
                        )
                    }

                    is WalkSummaryUiState.Error -> {
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

                    else -> {}
                }
            }
        }

        // Medal Celebration Overlay
        if (showCelebration && uiState is WalkSummaryUiState.Success) {
            val state = uiState as WalkSummaryUiState.Success
            MedalCelebrationOverlay(
                medalEmoji = state.medalEmoji ?: "🏅",
                animalName = state.animalName,
                onDismiss = { showCelebration = false }
            )
        }
    }

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.walk_discard_dialog_title)) },
            text = { Text(stringResource(R.string.walk_discard_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.discardWalk(walkId)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.walk_discard_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.walk_discard_cancel))
                }
            }
        )
    }
}

/**
 * Medal celebration overlay with confetti animation
 */
@Composable
private fun MedalCelebrationOverlay(
    medalEmoji: String,
    animalName: String,
    onDismiss: () -> Unit
) {
    // Medal bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "medal_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Confetti party configurations
    val party = listOf(
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x42e695, 0x3bb2b8),
            shapes = listOf(Shape.Circle, Shape.Square),
            size = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE),
            emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(100),
            position = Position.Relative(0.5, 0.3)
        ),
        Party(
            speed = 10f,
            maxSpeed = 50f,
            damping = 0.9f,
            angle = 270,
            spread = 90,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x42e695),
            shapes = listOf(Shape.Circle),
            size = listOf(Size.SMALL, Size.MEDIUM),
            emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(50),
            position = Position.Relative(0.0, 0.0)
        ),
        Party(
            speed = 10f,
            maxSpeed = 50f,
            damping = 0.9f,
            angle = 270,
            spread = 90,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x42e695),
            shapes = listOf(Shape.Circle),
            size = listOf(Size.SMALL, Size.MEDIUM),
            emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(50),
            position = Position.Relative(1.0, 0.0)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Konfetti animation
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = party
        )

        // Medal and text content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Congratulations text
            Text(
                text = "🎉 " + stringResource(R.string.medal_celebration_title) + " 🎉",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Medal with bounce animation
            Text(
                text = medalEmoji,
                fontSize = 120.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Medal earned text
            Text(
                text = stringResource(R.string.medal_celebration_message, animalName),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Continue button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700) // Gold color
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.medal_celebration_continue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Content for walk summary.
 *
 * Displays the walk route on a map, animal information with medal,
 * walk statistics, and action buttons for sharing, saving, or discarding.
 *
 * @param state Current success state with walk data
 * @param onSaveToHistory Callback when user taps save to history
 * @param onDiscard Callback when user taps discard
 * @param onShareToCommunity Callback when user taps share to community
 */
@Composable
private fun WalkSummaryContent(
    state: WalkSummaryUiState.Success,
    onSaveToHistory: () -> Unit,
    onDiscard: () -> Unit,
    onShareToCommunity: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Map with complete route
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            WalkSummaryMap(routePoints = state.routePoints)
        }

        // Summary card
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
                // Animal info with medal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = state.animalImageUrl,
                        contentDescription = state.animalName,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.animalName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Medal display
                        state.medalEmoji?.let { emoji ->
                            Text(
                                text = "$emoji ${stringResource(R.string.walk_medal_earned)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // Stats
                WalkSummaryStat(
                    label = stringResource(R.string.walk_duration_label),
                    value = state.formattedDuration
                )

                Spacer(modifier = Modifier.height(8.dp))

                WalkSummaryStat(
                    label = stringResource(R.string.walk_distance_label),
                    value = state.formattedDistance
                )

                Spacer(modifier = Modifier.height(8.dp))

                WalkSummaryStat(
                    label = stringResource(R.string.walk_date_label),
                    value = state.walk.date
                )
            }
        }

        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Share to community
            Button(
                onClick = onShareToCommunity,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.walk_share_community))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save to personal history
            Button(
                onClick = onSaveToHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.walk_save_history))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Discard walk
            OutlinedButton(
                onClick = onDiscard,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.walk_discard_button))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Google Maps component for walk summary
 */
@Composable
private fun WalkSummaryMap(routePoints: List<LatLng>) {
    if (routePoints.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.walk_no_route_data))
        }
        return
    }

    // Calculate bounds to fit all route points
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            routePoints.first(),
            15f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true
        )
    ) {
        // Draw route
        Polyline(
            points = routePoints,
            color = Color.Blue,
            width = 10f
        )

        // Start marker
        Marker(
            state = MarkerState(position = routePoints.first()),
            title = "Start"
        )

        // End marker
        Marker(
            state = MarkerState(position = routePoints.last()),
            title = "End"
        )
    }
}

/**
 * Single stat row in summary
 */
@Composable
private fun WalkSummaryStat(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}