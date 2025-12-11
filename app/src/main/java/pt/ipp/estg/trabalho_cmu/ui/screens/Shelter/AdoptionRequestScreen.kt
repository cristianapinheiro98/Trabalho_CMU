package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
 *
 * The screen automatically loads adoption requests for the currently authenticated shelter.
 *
 * Behavior:
 * - Shows a loading indicator while data is being fetched
 * - Displays an empty state message when there are no pending requests
 * - Shows a list of request cards when requests exist
 * - Approval/rejection updates local and remote data through the ViewModel
 *
 * @param onNavigateBack Callback triggered when the back button is pressed
 * @param authViewModel ViewModel holding current authenticated shelter information
 * @param shelterMngViewModel ViewModel responsible for managing requests and shelter actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestScreen(
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
    val scrollState = rememberScrollState()

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
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (requests.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_pending_requests),
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 40.dp)
                        )
                    } else {
                        requests.forEach { request ->
                            OwnershipRequestCard(
                                request = request,
                                onApprove = { shelterMngViewModel.approveRequest(request) },
                                onReject = { shelterMngViewModel.rejectRequest(request) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    message?.let {
        AlertDialog(
            onDismissRequest = { shelterMngViewModel.clearMessage() },
            confirmButton = {
                TextButton(onClick = { shelterMngViewModel.clearMessage() }) { Text("OK") }
            },
            title = { Text(stringResource(R.string.success_in_request)) },
            text = { Text(it) }
        )
    }

    error?.let {
        AlertDialog(
            onDismissRequest = { shelterMngViewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { shelterMngViewModel.clearError() }) { Text("OK") }
            },
            title = { Text(stringResource(R.string.error_in_request)) },
            text = { Text(it) }
        )
    }
}

/**
 * Card showing the details of one adoption request and buttons to approve/reject it.
 */
@Composable
fun OwnershipRequestCard(
    request: AdoptionRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.request_label),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = onReject) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.reject_request_description),
                            tint = Color(0xFFD32F2F)
                        )
                    }
                    IconButton(onClick = onApprove) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.approve_request_description),
                            tint = Color(0xFF388E3C)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                Text(text = request.animal)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdoptionRequestPreview() {
    val requests = listOf(
        AdoptionRequest("001", "João Sousa", "joao@example.com", "Luna"),
        AdoptionRequest("002", "Ana Costa", "ana@example.com", "Max")
    )

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            requests.forEach {
                OwnershipRequestCard(request = it, onApprove = {}, onReject = {})
            }
        }
    }
}
