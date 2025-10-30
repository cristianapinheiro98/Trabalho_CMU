package pt.ipp.estg.trabalho_cmu.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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

data class PedidoAdocao(
    val nome: String,
    val email: String,
    val animal: String,
    val id: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequest(
    onAcceptanceSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val pedidos = remember {
        mutableStateListOf(
            PedidoAdocao("José Lemos", "joselemos@example.com", "Bolinhas", "512549462689496"),
            PedidoAdocao("Maria Silva", "maria@example.com", "Luna", "875694236875432")
        )
    }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔹 Linha superior com o título e o botão "Voltar"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color(0xFF37474F)
                )
            }
            Text(
                text = "Pedidos de Adoção",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F),
                modifier = Modifier.padding(end = 32.dp)
            )
        }

        if (pedidos.isEmpty()) {
            Text(
                text = "Sem pedidos pendentes",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 40.dp)
            )
        }

        pedidos.forEach { pedido ->
            PedidoCard(
                pedido = pedido,
                onAprovar = {
                    pedidos.remove(pedido)
                    dialogMessage = "Pedido de ${pedido.nome} aceite com sucesso!"
                    isSuccessDialog = true
                    showDialog = true
                },
                onRejeitar = {
                    pedidos.remove(pedido)
                    dialogMessage = "Pedido de ${pedido.nome} rejeitado!"
                    isSuccessDialog = false
                    showDialog = true
                }
            )
        }
    }

    // ✅ Dialog de confirmação
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        if (isSuccessDialog) onAcceptanceSuccess()
                    }
                ) { Text("OK") }
            },
            title = { Text(if (isSuccessDialog) "Sucesso" else "Aviso") },
            text = { Text(dialogMessage) }
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
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(pedido.nome, fontWeight = FontWeight.Bold)
            Text(pedido.email)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Nome Animal: ${pedido.animal}")
            Text("ID: ${pedido.id}")
        }
    }
}
