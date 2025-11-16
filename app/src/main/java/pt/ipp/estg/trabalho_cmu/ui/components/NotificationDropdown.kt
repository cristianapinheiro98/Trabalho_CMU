package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)

// TODO: Implement notification logic based on user type:
// - ADMIN: Receives notifications for new ownership requests
// - USER: Receives notifications for new animals added and ownership approved/rejected

// Mock data
fun getMockNotifications() = listOf(
    // ADMIN type notifications
    Notification(1, "Novo pedido de adoção", "João Silva enviou pedido para adotar o Max", "Há 5 minutos"),
    Notification(2, "Pedido pendente", "Maria Santos aguarda resposta sobre adoção da Luna", "Há 1 hora"),

    // USER type notifications
    Notification(3, "Pedido aprovado!", "O seu pedido de adoção do Rex foi aprovado pelo abrigo", "Há 2 horas"),
    Notification(4, "Novo animal disponível", "Golden Retriever de 2 anos foi adicionado ao catálogo", "Há 3 horas"),
    Notification(5, "Pedido rejeitado", "Infelizmente o seu pedido para Bella foi rejeitado", "Há 5 horas"),
    Notification(6, "Novos animais", "3 gatos foram adicionados no Abrigo de Felgueiras", "Há 1 dia"),

    // Other notifications
    Notification(7, "Lembrete de atividade", "Passeio com Molly agendado para amanhã às 10h", "Há 2 dias")
)

@Composable
fun NotificationDropdown() {
    var expanded by remember { mutableStateOf(false) }
    val notifications = remember { getMockNotifications() }

    Box {
        // Notification icon with badge
        BadgedBox(
            badge = {
                if (notifications.isNotEmpty()) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ) {
                        Text(notifications.size.toString())
                    }
                }
            }
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificações",
                    tint = Color(0xFF37474F)
                )
            }
        }

        // Dropdown with notifications
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = (-100).dp, y = 0.dp),
            modifier = Modifier.width(320.dp)
        ) {
            // Header
            Text(
                text = "Notificações",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider()

            if (notifications.isEmpty()) {
                Text(
                    text = "Sem notificações",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            } else {
                // Column with scroll instead of LazyColumn
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    notifications.forEach { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                // TODO: Navigate to relevant screen based on notification type
                                expanded = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = notification.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = notification.timestamp,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = notification.message,
            fontSize = 13.sp,
            color = Color.DarkGray
        )
    }
}