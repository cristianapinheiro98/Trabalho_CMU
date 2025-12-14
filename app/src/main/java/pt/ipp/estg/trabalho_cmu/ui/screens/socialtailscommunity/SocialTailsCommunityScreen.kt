package pt.ipp.estg.trabalho_cmu.ui.screens.socialtailscommunity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk
import pt.ipp.estg.trabalho_cmu.ui.components.OfflineDialog
import pt.ipp.estg.trabalho_cmu.ui.components.PodiumSection
import pt.ipp.estg.trabalho_cmu.ui.components.PublicWalkCard
import pt.ipp.estg.trabalho_cmu.ui.components.WalkMapModal

/**
 * SocialTails Community Screen.
 *
 * Displays the community feed with:
 * - All-time podium showing top 3 walks by duration
 * - Monthly podium showing top 3 walks of the current month
 * - Paginated list of public walks shared by all users
 *
 * Each walk in the feed shows the animal, owner, distance, and a mini map
 * that can be expanded to view the full route interactively.
 *
 * @param navController Navigation controller for handling back navigation
 * @param viewModel ViewModel managing the community data state
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SocialTailsCommunityScreen(
    windowSize: WindowWidthSizeClass,
    navController: NavController,
    viewModel: SocialTailsCommunityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.observeAsState(SocialTailsCommunityUiState.Initial)

    // State for map modal
    var selectedWalkForMap by remember { mutableStateOf<Walk?>(null) }

    // Load data when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadCommunityData()
    }

    // Handle offline state with dialog
    if (uiState is SocialTailsCommunityUiState.Offline) {
        OfflineDialog(
            onDismiss = { navController.navigateUp() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is SocialTailsCommunityUiState.Initial,
            is SocialTailsCommunityUiState.Loading -> {
                LoadingContent()
            }

            is SocialTailsCommunityUiState.Offline -> {
                // Dialog is shown above, show empty content behind
                Box(modifier = Modifier.fillMaxSize())
            }

            is SocialTailsCommunityUiState.Success -> {
                SuccessContent(
                    state = state,
                    viewModel = viewModel,
                    windowSize = windowSize,
                    onMapClick = { walk -> selectedWalkForMap = walk },
                    onLoadMore = { viewModel.loadMoreWalks() }
                )
            }

            is SocialTailsCommunityUiState.Error -> {
                ErrorContent(message = state.message)
            }
        }
    }

    // Map modal
    selectedWalkForMap?.let { walk ->
        WalkMapModal(
            walk = walk,
            formattedDistance = viewModel.formatDistance(walk.distance),
            formattedDuration = viewModel.formatDuration(walk.duration),
            onDismiss = { selectedWalkForMap = null }
        )
    }
}

/**
 * Loading state content with centered progress indicator.
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
 * Error state content with centered error message.
 *
 * @param message Error message to display
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Success state content with podiums and public walks feed.
 *
 * @param state Current success state with all community data
 * @param viewModel ViewModel for formatting functions
 * * @param widthSizeClass Window width size class to adapt phone/tablet layout
 * @param onMapClick Callback when user taps a walk's map
 * @param onLoadMore Callback to load more walks for pagination
 */
@Composable
private fun SuccessContent(
    state: SocialTailsCommunityUiState.Success,
    viewModel: SocialTailsCommunityViewModel,
    windowSize: WindowWidthSizeClass,
    onMapClick: (Walk) -> Unit,
    onLoadMore: () -> Unit
) {
    val isTablet = windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded

    val listState = rememberLazyListState()

    // Detect when user scrolls near the end for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 2 && state.hasMoreWalks && !state.isLoadingMore
        }
    }

    // Trigger load more when scrolled near end
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    val contentPadding = if (isTablet) 24.dp else 16.dp
    val titleTopPadding = if (isTablet) 16.dp else 0.dp
    val maxWidth = if (isTablet) 1200.dp else 600.dp

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal=contentPadding)
            .widthIn(max=maxWidth),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen title
        item {
            Text(
                text = stringResource(R.string.socialtails_community_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = titleTopPadding)
            )
        }

        if(!isTablet){
        // All-time podium
        item {
            PodiumSection(
                title = stringResource(R.string.podium_all_time),
                walks = state.topWalksAllTime,
                formatDuration = { viewModel.formatDuration(it) }
            )
        }

        // Monthly podium
            item {
                PodiumSection(
                    title = stringResource(R.string.podium_monthly, state.currentMonthName),
                    walks = state.topWalksMonthly,
                    formatDuration = { viewModel.formatDuration(it) }
                )
            }
        }else{
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PodiumSection(
                            title = stringResource(R.string.podium_all_time),
                            walks = state.topWalksAllTime,
                            formatDuration = { viewModel.formatDuration(it) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PodiumSection(
                            title = stringResource(R.string.podium_monthly, state.currentMonthName),
                            walks = state.topWalksMonthly,
                            formatDuration = { viewModel.formatDuration(it) }
                        )
                    }
                }
            }
        }

        // Feed section title
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.community_feed_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Public walks feed
        if (state.publicWalks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.community_no_walks),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(
                items = state.publicWalks,
                key = { it.id }
            ) { walk ->
                PublicWalkCard(
                    walk = walk,
                    relativeDate = viewModel.formatRelativeDate(walk.date),
                    formattedDistance = viewModel.formatDistance(walk.distance),
                    onMapClick = { onMapClick(walk) }
                )
            }
        }

        // Loading more indicator
        if (state.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // End of list spacer
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}