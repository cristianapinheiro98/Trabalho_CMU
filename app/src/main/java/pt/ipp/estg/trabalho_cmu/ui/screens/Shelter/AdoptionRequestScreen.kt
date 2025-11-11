package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import pt.ipp.estg.trabalho_cmu.data.models.PedidoAdocao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ShelterMngViewModel = viewModel()
) {
    val pedidos by viewModel.pedidos.observeAsState(emptyList())
    val message by viewModel.message.observeAsState()
    val error by viewModel.error.observeAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pedidos.isEmpty()) {
            Text(
                text = "Sem pedidos pendentes",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            pedidos.forEach { pedido ->
                PedidoCard(
                    pedido = pedido,
                    onAprovar = { viewModel.approveRequest(pedido) },
                    onRejeitar = { viewModel.rejectRequest(pedido) }
                )
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
    pedido: PedidoAdocao,
    onAprovar: () -> Unit,
    onRejeitar: () -> Unit
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
            IconButton(onClick = onAprovar) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Aprovar Pedido",
                    tint = Color(0xFF388E3C)
                )
            }
            IconButton(onClick = onRejeitar) {
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
            Text(pedido.nome, fontWeight = FontWeight.Bold)
            Text(pedido.email)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Animal: ${pedido.animal}")
            Text("ID: ${pedido.id}")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdoptionRequestPreview() {
    val pedidos = listOf(
        PedidoAdocao("001", "João Sousa", "joao@example.com", "Luna"),
        PedidoAdocao("002", "Ana Costa", "ana@example.com", "Max")
    )

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            pedidos.forEach {
                PedidoCard(pedido = it, onAprovar = {}, onRejeitar = {})
            }
        }
    }
}
