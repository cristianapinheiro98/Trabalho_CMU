package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    isLoggedIn: Boolean,
    isAdmin: Boolean,
    currentRoute: String?,
    onMenuClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onVeterinariansClick: () -> Unit
) {
    // 🔹 Mostrar seta apenas nos ecrãs de detalhe do admin
    val showBackArrow = isAdmin && currentRoute in listOf("AnimalCreation", "AdoptionRequest")

    // 🔹 Mostrar menu apenas para utilizadores normais
    val showMenuIcon = isLoggedIn && !isAdmin && !showBackArrow

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_collar),
                    contentDescription = "Tailwagger logo",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "Tailwagger",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
        },
        navigationIcon = {
            when {
                showBackArrow -> {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color(0xFF37474F)
                        )
                    }
                }
                showMenuIcon -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir menu",
                            tint = Color(0xFF37474F)
                        )
                    }
                }
            }
        },
        actions = {
            if (isLoggedIn) {
                // 💉 Ícone de veterinários — só aparece para admin
                if (isAdmin) {
                    IconButton(onClick = onVeterinariansClick) {
                        Icon(
                            imageVector = Icons.Outlined.Vaccines,
                            contentDescription = "Lista de Veterinários",
                            tint = Color(0xFF37474F)
                        )
                    }
                }

                // 🔔 Notificações — só aparecem para user normal
                if (!isAdmin) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificações",
                            tint = Color(0xFF37474F)
                        )
                    }
                }

                // 🚪 Logout — aparece para ambos
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFF37474F)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
    )
}
