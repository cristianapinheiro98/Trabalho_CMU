package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
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
import androidx.compose.ui.res.stringResource
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


    val showMenuIcon = isLoggedIn && !isAdmin

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
                    text = stringResource(id = R.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
        },
        navigationIcon = {
            when {
                showMenuIcon -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.open_menu),
                            tint = Color(0xFF37474F)
                        )
                    }
                }
            }
        },
        actions = {
            if (isLoggedIn) {
                if (isAdmin) {
                    IconButton(onClick = onVeterinariansClick) {
                        Icon(
                            imageVector = Icons.Outlined.Vaccines,
                            contentDescription = stringResource(R.string.veterinaries_list),
                            tint = Color(0xFF37474F)
                        )
                    }
                }

                if (!isAdmin) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = stringResource(R.string.notifications),
                            tint = Color(0xFF37474F)
                        )
                    }
                }

                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription =stringResource(R.string.logout),
                        tint = Color(0xFF37474F)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}
