package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

/**
 * Screen responsible for displaying and managing adoption (ownership) requests submitted
 * by users to a specific shelter. It allows shelter administrators to:
 *
 *  - View all pending requests
 *  - Approve or reject each request
 *  - Receive feedback via dialogs when a request is processed
 *  - Responsive layout based on device size
 *
 * The screen automatically loads adoption requests for the currently authenticated shelter.
 *
 * @param windowSize Size class of the device window
 * @param onNavigateBack Callback triggered when the back button is pressed
 * @param authViewModel ViewModel holding current authenticated shelter information
 * @param shelterMngViewModel ViewModel responsible for managing requests and shelter actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestScreen(
    windowSize: WindowWidthSizeClass,
    onNavigateBack: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    shelterMngViewModel: ShelterMngViewModel = viewModel()
) {
    val currentShelter by authViewModel.currentShelter.observeAsState()

    LaunchedEffect(currentShelter) {
        currentShelter?.let {
            shelterMngViewModel.setShelterFirebaseUid(it.id)
        }
    }

    val requests by shelterMngViewModel.requests.observeAsState(emptyList())
    val message by shelterMngViewModel.message.observeAsState()
    val error by shelterMngViewModel.error.observeAsState()
    val uiState by shelterMngViewModel.uiState.observeAsState(ShelterMngUiState.Initial)

    val isLoading = uiState is ShelterMngUiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adoption_requests_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        AdoptionRequestContent(
            windowSize = windowSize,
            requests = requests,
            isLoading = isLoading,
            onApprove = { shelterMngViewModel.approveRequest(it) },
            onReject = { shelterMngViewModel.rejectRequest(it) },
            modifier = Modifier.padding(paddingValues)
        )

        ResultDialogs(
            message = message,
            error = error,
            onClearMessage = { shelterMngViewModel.clearMessage() },
            onClearError = { shelterMngViewModel.clearError() }
        )
    }
}

@Composable
private fun AdoptionRequestContent(
    windowSize: WindowWidthSizeClass,
    requests: List<AdoptionRequest>,
    isLoading: Boolean,
    onApprove: (AdoptionRequest) -> Unit,
    onReject: (AdoptionRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTablet = windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded
    val contentPadding = if (isTablet) 32.dp else 16.dp
    val maxWidth = if (isTablet) 1000.dp else 600.dp
    val emptyTextSize = if (isTablet) 20.sp else 16.sp

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (requests.isEmpty()) {
            EmptyRequestsMessage(emptyTextSize, contentPadding)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isTablet) {
                    TabletRequestsList(
                        requests = requests,
                        maxWidth = maxWidth,
                        contentPadding = contentPadding,
                        onApprove = onApprove,
                        onReject = onReject
                    )
                } else {
                    PhoneRequestsList(
                        requests = requests,
                        contentPadding = contentPadding,
                        onApprove = onApprove,
                        onReject = onReject
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRequestsMessage(
    textSize: androidx.compose.ui.unit.TextUnit,
    padding: androidx.compose.ui.unit.Dp
) {
    Text(
        text = stringResource(R.string.no_pending_requests),
        color = Color.Gray,
        fontSize = textSize,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(padding)
    )
}

@Composable
private fun TabletRequestsList(
    requests: List<AdoptionRequest>,
    maxWidth: androidx.compose.ui.unit.Dp,
    contentPadding: androidx.compose.ui.unit.Dp,
    onApprove: (AdoptionRequest) -> Unit,
    onReject: (AdoptionRequest) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth(),
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(requests) { request ->
            OwnershipRequestCard(
                request = request,
                onApprove = { onApprove(request) },
                onReject = { onReject(request) },
                isTablet = true
            )
        }
    }
}

@Composable
private fun PhoneRequestsList(
    requests: List<AdoptionRequest>,
    contentPadding: androidx.compose.ui.unit.Dp,
    onApprove: (AdoptionRequest) -> Unit,
    onReject: (AdoptionRequest) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        requests.forEach { request ->
            OwnershipRequestCard(
                request = request,
                onApprove = { onApprove(request) },
                onReject = { onReject(request) },
                isTablet = false
            )
        }
    }
}

@Composable
fun OwnershipRequestCard(
    request: AdoptionRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isTablet: Boolean
) {
    val cardPadding = if (isTablet) 24.dp else 16.dp
    val titleSize = if (isTablet) 20.sp else 18.sp
    val bodySize = if (isTablet) 16.sp else 14.sp
    val iconSize = if (isTablet) 28.dp else 24.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTablet) 6.dp else 4.dp)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.request_label),
                    fontWeight = FontWeight.Bold,
                    fontSize = titleSize,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp)) {
                    IconButton(onClick = onReject) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.reject_request_description),
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    IconButton(onClick = onApprove) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.approve_request_description),
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = if (isTablet) 12.dp else 8.dp))

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = request.nome,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = bodySize
                        )
                        Text(
                            text = request.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = bodySize
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.animal_label) + " ",
                                fontWeight = FontWeight.Bold,
                                fontSize = bodySize
                            )
                            Text(
                                text = request.animal,
                                fontSize = bodySize
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = request.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = request.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.animal_label),
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = " ${request.animal}")
                }
            }
        }
    }
}

@Composable
private fun ResultDialogs(
    message: String?,
    error: String?,
    onClearMessage: () -> Unit,
    onClearError: () -> Unit
) {
    message?.let {
        AlertDialog(
            onDismissRequest = onClearMessage,
            confirmButton = {
                TextButton(onClick = onClearMessage) { Text("OK") }
            },
            title = { Text(stringResource(R.string.success_in_request)) },
            text = { Text(it) }
        )
    }

    error?.let {
        AlertDialog(
            onDismissRequest = onClearError,
            confirmButton = {
                TextButton(onClick = onClearError) { Text("OK") }
            },
            title = { Text(stringResource(R.string.error_in_request)) },
            text = { Text(it) }
        )
    }
}

@Preview(name = "Phone", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun AdoptionRequestPhonePreview() {
    val requests = listOf(
        AdoptionRequest("001", "João Sousa", "joao@example.com", "Luna"),
        AdoptionRequest("002", "Ana Costa", "ana@example.com", "Max")
    )

    MaterialTheme {
        AdoptionRequestContent(
            windowSize = WindowWidthSizeClass.Compact,
            requests = requests,
            isLoading = false,
            onApprove = {},
            onReject = {}
        )
    }
}

@Preview(name = "Tablet", widthDp = 900, heightDp = 1280, showBackground = true)
@Composable
fun AdoptionRequestTabletPreview() {
    val requests = listOf(
        AdoptionRequest("001", "João Sousa", "joao@example.com", "Luna"),
        AdoptionRequest("002", "Ana Costa", "ana@example.com", "Max"),
        AdoptionRequest("003", "Carlos Silva", "carlos@example.com", "Rex")
    )

    MaterialTheme {
        AdoptionRequestContent(
            windowSize = WindowWidthSizeClass.Expanded,
            requests = requests,
            isLoading = false,
            onApprove = {},
            onReject = {}
        )
    }
}
