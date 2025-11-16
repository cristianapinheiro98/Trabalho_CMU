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
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

/**
 * Screen that displays all pending adoption requests for a shelter
 * and allows approving or rejecting each one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestScreen(
    onNavigateBack: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    shelterMngViewModel: ShelterMngViewModel = viewModel()
) {
    val currentShelter by authViewModel.currentShelter.observeAsState()
    val accountType by authViewModel.accountType.observeAsState()

    LaunchedEffect(accountType) {
        when (accountType) {
            AccountType.SHELTER -> {
                currentShelter?.let { shelter ->
                    println("[AdoptionRequest] Shelter: ${shelter.name}, Firebase UID: ${shelter.firebaseUid}")
                    shelterMngViewModel.setShelterFirebaseUid(shelter.firebaseUid)
                }
            }
            else -> {
                println("Only Shelters can view adoption requests")
            }
        }
    }

    val requests by shelterMngViewModel.requests.observeAsState(emptyList())
    val message by shelterMngViewModel.message.observeAsState()
    val error by shelterMngViewModel.error.observeAsState()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
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
                    PedidoCard(
                        request = request,
                        onApprove = { shelterMngViewModel.approveRequest(request) },
                        onReject = { shelterMngViewModel.rejectRequest(request) }
                    )
                }
            }
        }
    }

    // Success dialog
    message?.let {
        AlertDialog(
            onDismissRequest = { shelterMngViewModel.clearMessage() },
            confirmButton = {
                TextButton(onClick = { shelterMngViewModel.clearMessage() }) {
                    Text(stringResource(R.string.dialog_ok_button))
                }
            },
            title = { Text(stringResource(R.string.dialog_warning_title)) },
            text = { Text(it) }
        )
    }

    // Error dialog
    error?.let {
        AlertDialog(
            onDismissRequest = { shelterMngViewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { shelterMngViewModel.clearError() }) {
                    Text(stringResource(R.string.dialog_ok_button))
                }
            },
            title = { Text(stringResource(R.string.dialog_error_title)) },
            text = { Text(it) }
        )
    }
}

/**
 * Card showing the details of one adoption request and buttons to approve/reject it.
 */
@Composable
fun PedidoCard(
    request: AdoptionRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.request_label),
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF455A64)
        )

        Row {
            IconButton(onClick = onApprove) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.approve_request_description),
                    tint = Color(0xFF388E3C)
                )
            }
            IconButton(onClick = onReject) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.reject_request_description),
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(request.nome, fontWeight = FontWeight.Bold)
            Text(request.email)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.animal_label) + " " + request.animal)
            Text(stringResource(R.string.id_label) + " " + request.id)
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
                PedidoCard(request = it, onApprove = {}, onReject = {})
            }
        }
    }
}
