package pt.ipp.estg.trabalho_cmu.ui.screens.User

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.services.WalkTrackingService
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalSelectionDialog
import androidx.compose.ui.graphics.Color

/**
 * Main Options Screen (User Dashboard)
 * Displays user's animals, recent medals, last walk, and action buttons
 * Follows MVVM pattern - all state is managed by ViewModel
 *
 * @param navController Navigation controller for screen navigation
 * @param windowSize Window size class for adaptive layouts
 * @param viewModel ViewModel managing screen state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainOptionsScreen(
    navController: NavController,
    windowSize: WindowWidthSizeClass,
    viewModel: MainOptionsViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observe ViewModel states
    val uiState by viewModel.uiState.observeAsState(MainOptionsUiState.Initial)
    val dialogState by viewModel.dialogState.observeAsState(DialogState())
    val isWalkActive by viewModel.isWalkActive.observeAsState(false)

    // Service connection for checking active walk (Android-specific UI concern)
    var trackingService by remember { mutableStateOf<WalkTrackingService?>(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WalkTrackingService.LocalBinder
                trackingService = binder.getService()
                viewModel.checkActiveWalk(trackingService)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                trackingService = null
            }
        }
    }

    // Bind to service to check if walk is active
    DisposableEffect(Unit) {
        val intent = Intent(context, WalkTrackingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            try {
                context.unbindService(serviceConnection)
            } catch (e: IllegalArgumentException) {
                // Service not bound, ignore
            }
        }
    }

    // Load data when screen loads
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        topBar = {
            MainOptionsTopBar(
                isWalkActive = isWalkActive,
                onActiveWalkClick = {
                    viewModel.getActiveWalkNavigationRoute()?.let { route ->
                        navController.navigate(route)
                    }
                },
                onSettingsClick = { navController.navigate("Preferences") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MainOptionsUiState.Initial,
                is MainOptionsUiState.Loading -> {
                    LoadingContent()
                }

                is MainOptionsUiState.Success -> {
                    MainOptionsContent(
                        state = state,
                        isWalkActive = isWalkActive,
                        windowSize = windowSize,
                        onScheduleVisitClick = { viewModel.onScheduleVisitClick() },
                        onStartWalkClick = {
                            if (isWalkActive) {
                                viewModel.getActiveWalkNavigationRoute()?.let { route ->
                                    navController.navigate(route)
                                }
                            } else {
                                viewModel.onStartWalkClick()
                            }
                        },
                        onSchedulesClick = { navController.navigate("ActivitiesHistory") },
                        onWalkHistoryClick = {
                            navController.navigate(viewModel.getWalkHistoryNavigationRoute())
                        },
                        onCommunityClick = { navController.navigate("SocialTailsCommunity") },
                        onViewMedalsClick = { viewModel.onViewMedalsClick() },
                        onMedalClick = { walkId ->
                            navController.navigate(viewModel.getWalkHistoryNavigationRoute(walkId))
                        }
                    )
                }

                is MainOptionsUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadDashboardData() }
                    )
                }
            }
        }
    }

    // Dialogs - rendered based on ViewModel state
    DialogsContainer(
        uiState = uiState,
        dialogState = dialogState,
        onDismissAnimalSelection = { viewModel.dismissDialog() },
        onDismissNoAnimals = { viewModel.dismissNoAnimalsDialog() },
        onDismissMedalCollection = { viewModel.dismissMedalCollectionDialog() },
        onAnimalSelectedForSchedule = { animal ->
            navController.navigate(viewModel.getScheduleNavigationRoute(animal))
        },
        onAnimalSelectedForWalk = { animal ->
            navController.navigate(viewModel.getWalkNavigationRoute(animal))
        },
        onMedalClick = { walkId ->
            navController.navigate(viewModel.getWalkHistoryNavigationRoute(walkId))
        }
    )
}

/**
 * Top app bar for Main Options screen
 * Shows active walk indicator when applicable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainOptionsTopBar(
    isWalkActive: Boolean,
    onActiveWalkClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            if (isWalkActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onActiveWalkClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.app_name))
                    Spacer(modifier = Modifier.width(8.dp))
                    ActiveWalkIndicator()
                }
            } else {
                Text(stringResource(R.string.app_name))
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

/**
 * Visual indicator for active walk in progress
 */
@Composable
private fun ActiveWalkIndicator() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.walk_in_progress),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Loading state content
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state content with retry option
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry_button))
            }
        }
    }
}

/**
 * Main content for dashboard when data is loaded successfully
 */
@Composable
private fun MainOptionsContent(
    state: MainOptionsUiState.Success,
    isWalkActive: Boolean,
    windowSize: WindowWidthSizeClass,
    onScheduleVisitClick: () -> Unit,
    onStartWalkClick: () -> Unit,
    onSchedulesClick: () -> Unit,
    onWalkHistoryClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onViewMedalsClick: () -> Unit,
    onMedalClick: (String) -> Unit
) {
    val username = state.userName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Last walk section
        state.lastWalk?.let { lastWalk ->
            LastWalkCard(
                username = username,
                lastWalk = lastWalk
            )
        }

        // Recent medals section
        if (state.recentMedals.isNotEmpty()) {
            RecentMedalsCard(
                medals = state.recentMedals,
                onViewAllClick = onViewMedalsClick,
                onMedalClick = onMedalClick
            )
        }

        // Action buttons - adaptive layout based on window size
        ActionButtonsSection(
            windowSize = windowSize,
            isWalkActive = isWalkActive,
            onScheduleVisitClick = onScheduleVisitClick,
            onStartWalkClick = onStartWalkClick,
            onSchedulesClick = onSchedulesClick,
            onWalkHistoryClick = onWalkHistoryClick,
            onCommunityClick = onCommunityClick
        )
    }
}

/**
 * Action buttons section with colorful grid layout
 */
@Composable
private fun ActionButtonsSection(
    windowSize: WindowWidthSizeClass,
    isWalkActive: Boolean,
    onScheduleVisitClick: () -> Unit,
    onStartWalkClick: () -> Unit,
    onSchedulesClick: () -> Unit,
    onWalkHistoryClick: () -> Unit,
    onCommunityClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Schedule Visit + Start Walk
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onScheduleVisitClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9BD5) // Blue
                )
            ) {
                Text(stringResource(R.string.schedule_visit_button))
            }

            Button(
                onClick = onStartWalkClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF70AD47) // Green
                )
            ) {
                Text(
                    if (isWalkActive)
                        stringResource(R.string.walk_already_active)
                    else
                        stringResource(R.string.start_walk_button)
                )
            }
        }

        // Row 2: Schedules + Walk History
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSchedulesClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFED7D31) // Orange
                )
            ) {
                Text(stringResource(R.string.schedules_button))
            }

            Button(
                onClick = onWalkHistoryClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC000) // Yellow/Gold
                )
            ) {
                Text(stringResource(R.string.walk_history_button))
            }
        }

        // Row 3: Community (full width)
        Button(
            onClick = onCommunityClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9E5BD5) // Purple
            )
        ) {
            Text(stringResource(R.string.community_button))
        }
    }
}

/**
 * Card displaying information about the user's last walk with mini map
 */
@Composable
private fun LastWalkCard(
    username: String,
    lastWalk: LastWalkInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Greeting
            Text(
                text = stringResource(R.string.last_walk_greeting, username),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.last_walk_with),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Animal info + Map row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animal image + name (left side)
                Column(
                    modifier = Modifier.weight(0.4f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = lastWalk.animalImageUrl,
                        contentDescription = lastWalk.animalName,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = lastWalk.animalName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Mini map (right side) - takes remaining space
                if (lastWalk.routePoints.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        MiniWalkMap(routePoints = lastWalk.routePoints)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Distance info
            Text(
                text = stringResource(R.string.last_walk_distance, lastWalk.distance),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Mini interactive map showing the route of a walk
 */
@Composable
private fun MiniWalkMap(routePoints: List<LatLng>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            routePoints.first(),
            14f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false
        )
    ) {
        Polyline(
            points = routePoints,
            color = androidx.compose.ui.graphics.Color.Blue,
            width = 6f
        )
    }
}

/**
 * Card displaying recent medals earned by the user
 */
@Composable
private fun RecentMedalsCard(
    medals: List<MedalItem>,
    onViewAllClick: () -> Unit,
    onMedalClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.recent_medals_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Medals row with View All button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Medals (left side) - max 3
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    medals.take(3).forEach { medal ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onMedalClick(medal.walkId) }
                        ) {
                            Text(
                                text = medal.emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = medal.animalName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = medal.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // View All button (right side)
                TextButton(onClick = onViewAllClick) {
                    Text(stringResource(R.string.view_medal_collection))
                }
            }
        }
    }
}

/**
 * Single medal row in the medals card
 */
@Composable
private fun MedalRow(
    medal: MedalItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = medal.emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = medal.animalName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = medal.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Container for all dialogs - renders based on ViewModel dialog state
 */
@Composable
private fun DialogsContainer(
    uiState: MainOptionsUiState,
    dialogState: DialogState,
    onDismissAnimalSelection: () -> Unit,
    onDismissNoAnimals: () -> Unit,
    onDismissMedalCollection: () -> Unit,
    onAnimalSelectedForSchedule: (Animal) -> Unit,
    onAnimalSelectedForWalk: (Animal) -> Unit,
    onMedalClick: (String) -> Unit
) {
    // Animal selection dialog
    if (dialogState.isAnimalSelectionVisible) {
        val animals = when (dialogState.dialogType) {
            DialogType.SCHEDULE -> {
                (uiState as? MainOptionsUiState.Success)?.animals ?: emptyList()
            }
            DialogType.START_WALK -> {
                dialogState.availableAnimalsForWalk
            }
        }

        val title = when (dialogState.dialogType) {
            DialogType.SCHEDULE -> stringResource(R.string.schedule_visit_select_animal)
            DialogType.START_WALK -> stringResource(R.string.start_walk_select_animal)
        }

        val onAnimalSelected: (Animal) -> Unit = when (dialogState.dialogType) {
            DialogType.SCHEDULE -> onAnimalSelectedForSchedule
            DialogType.START_WALK -> onAnimalSelectedForWalk
        }

        AnimalSelectionDialog(
            animals = animals,
            title = title,
            isLoading = dialogState.isLoadingAnimals,
            onAnimalSelected = onAnimalSelected,
            onDismiss = onDismissAnimalSelection
        )
    }

    // No animals available dialog
    if (dialogState.isNoAnimalsVisible) {
        AlertDialog(
            onDismissRequest = onDismissNoAnimals,
            title = { Text(stringResource(R.string.start_walk_no_animals_title)) },
            text = { Text(stringResource(R.string.start_walk_no_animals_message)) },
            confirmButton = {
                TextButton(onClick = onDismissNoAnimals) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }

    // Medal collection dialog
    if (dialogState.isMedalCollectionVisible) {
        val medals = (uiState as? MainOptionsUiState.Success)?.recentMedals ?: emptyList()
        MedalCollectionDialog(
            medals = medals,
            onDismiss = onDismissMedalCollection,
            onMedalClick = onMedalClick
        )
    }
}

/**
 * Dialog displaying the user's medal collection
 */
@Composable
private fun MedalCollectionDialog(
    medals: List<MedalItem>,
    onDismiss: () -> Unit,
    onMedalClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.medal_collection_title)) },
        text = {
            if (medals.isEmpty()) {
                Text(stringResource(R.string.medal_collection_empty))
            } else {
                LazyColumn {
                    items(medals) { medal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMedalClick(medal.walkId) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = medal.emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = medal.animalName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = medal.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_button))
            }
        }
    )
}