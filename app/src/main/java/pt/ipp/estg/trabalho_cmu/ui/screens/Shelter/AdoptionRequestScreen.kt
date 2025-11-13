package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.data.models.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestScreen(
    onNavigateBack: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    viewModel: ShelterMngViewModel = viewModel()
) {
    val currentShelter by authViewModel.currentShelter.observeAsState()
    val accountType by authViewModel.accountType.observeAsState()

    LaunchedEffect(accountType) {
        when (accountType) {
            pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType.SHELTER -> {
                currentShelter?.let { shelter ->
                    println("[AdoptionRequest] Shelter: ${shelter.name}, ID: ${shelter.id}")
                    viewModel.setShelterId(shelter.id)
                }
            }
            else -> {
                println(" Apenas Shelters podem ver pedidos de adoção")
            }
        }
    }

    val request by viewModel.requests.observeAsState(emptyList())
    val message by viewModel.message.observeAsState()
    val error by viewModel.error.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedidos de Adoção") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
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
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (request.isEmpty()) {
                Text(
                    text = "Sem pedidos pendentes",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                request.forEach { pedido ->
                    PedidoCard(
                        request = pedido,
                        onApprove = { viewModel.approveRequest(pedido) },
                        onReject = { viewModel.rejectRequest(pedido) }
                    )
                }
            }
        }
    }

    // 🔹 Diálogo de sucesso/erro
    message?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") }
            },
            title = { Text("Aviso") },
            text = { Text(it) }
        )
    }

    error?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            },
            title = { Text("Erro") },
            text = { Text(it) }
        )
    }
}

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
            text = "Pedido",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color(0xFF455A64)
        )

        Row {
            IconButton(onClick = onApprove) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Aprovar Pedido",
                    tint = Color(0xFF388E3C)
                )
            }
            IconButton(onClick = onReject) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Rejeitar Pedido",
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
            Text("Animal: ${request.animal}")
            Text("ID: ${request.id}")
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
