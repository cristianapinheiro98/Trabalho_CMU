package pt.ipp.estg.trabalho_cmu.ui.screens.walk.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R

/**
 * Screen for walk history with pagination
 * Displays paginated list of walks with interactive maps
 *
 * @param navController Navigation controller
 * @param scrollToWalkId Optional walk ID to scroll to (from medal collection)
 * @param viewModel Walk history view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkHistoryScreen(
    navController: NavController,
    scrollToWalkId: String? = null,
    viewModel: WalkHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.observeAsState(WalkHistoryUiState.Initial)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Load walk history when screen loads
    LaunchedEffect(Unit) {
        viewModel.loadWalkHistory(scrollToWalkId)
    }

    // Handle scroll to specific walk
    LaunchedEffect(uiState) {
        if (uiState is WalkHistoryUiState.Success) {
            val successState = uiState as WalkHistoryUiState.Success
            successState.scrollToWalkId?.let { targetWalkId ->
                val index = successState.walks.indexOfFirst { it.walkId == targetWalkId }
                if (index != -1) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(index)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.walk_history_title)) },
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
                is WalkHistoryUiState.Initial,
                is WalkHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is WalkHistoryUiState.Offline -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = stringResource(R.string.walk_history_offline_message),
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        // Show cached data if available
                    }
                }

                is WalkHistoryUiState.Success -> {
                    WalkHistoryList(
                        walks = state.walks,
                        hasMore = state.hasMore,
                        listState = listState,
                        onLoadMore = { viewModel.loadMoreWalks() }
                    )
                }

                is WalkHistoryUiState.LoadingMore -> {
                    // Show current list with loading indicator at bottom
                    val successState = uiState as? WalkHistoryUiState.Success
                    if (successState != null) {
                        WalkHistoryList(
                            walks = successState.walks,
                            hasMore = true,
                            listState = listState,
                            onLoadMore = { viewModel.loadMoreWalks() },
                            isLoadingMore = true
                        )
                    }
                }

                is WalkHistoryUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.walk_history_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is WalkHistoryUiState.Error -> {
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
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text(stringResource(R.string.retry_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Paginated list of walk history items
 */
@Composable
private fun WalkHistoryList(
    walks: List<WalkHistoryItem>,
    hasMore: Boolean,
    listState: LazyListState,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean = false
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(walks, key = { it.walkId }) { walk ->
            WalkHistoryItemCard(walk)
        }

        // Load more trigger
        if (hasMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingMore) {
                        CircularProgressIndicator()
                    } else {
                        Button(onClick = onLoadMore) {
                            Text(stringResource(R.string.walk_history_load_more))
                        }
                    }
                }
            }
        }
    }

    // Auto-load more when scrolled near bottom
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null &&
                    lastVisibleItem.index >= walks.size - 1 &&
                    hasMore &&
                    !isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }
}

/**
 * Individual walk history item card with interactive map
 */
@Composable
private fun WalkHistoryItemCard(walk: WalkHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Interactive map
            if (walk.routePoints.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    WalkHistoryMap(routePoints = walk.routePoints)
                }
            }

            // Walk info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animal thumbnail
                AsyncImage(
                    model = walk.animalImageUrl,
                    contentDescription = walk.animalName,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Walk details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = walk.animalName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        walk.medalEmoji?.let { emoji ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${stringResource(R.string.walk_duration_label)}: ${walk.duration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${stringResource(R.string.walk_distance_label)}: ${walk.distance}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${stringResource(R.string.walk_date_label)}: ${walk.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Interactive map for walk history item
 */
@Composable
private fun WalkHistoryMap(routePoints: List<LatLng>) {
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
        // Draw route
        Polyline(
            points = routePoints,
            color = Color.Blue,
            width = 8f
        )
    }
}