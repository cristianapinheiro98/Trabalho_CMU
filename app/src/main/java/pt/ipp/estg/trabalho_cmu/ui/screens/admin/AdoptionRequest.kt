package pt.ipp.estg.trabalho_cmu.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ipp.estg.trabalho_cmu.data.models.PedidoAdocao

@Composable
fun AdoptionRequest(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.pedidos.isEmpty()) {
            Text(
                text = "Sem pedidos pendentes",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            state.pedidos.forEach { pedido ->
                PedidoCard(
                    pedido = pedido,
                    onAprovar = { viewModel.aprovarPedido(pedido) },
                    onRejeitar = { viewModel.rejeitarPedido(pedido) }
                )
            }
        }
    }

    state.dialogMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.fecharDialogo() },
            confirmButton = {
                TextButton(onClick = { viewModel.fecharDialogo() }) { Text("OK") }
            },
            title = { Text(if (state.isSuccessDialog) "Sucesso" else "Aviso") },
            text = { Text(msg) }
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
            Text("Nome Animal: ${pedido.animal}")
            Text("ID: ${pedido.id}")
        }
    }
}
