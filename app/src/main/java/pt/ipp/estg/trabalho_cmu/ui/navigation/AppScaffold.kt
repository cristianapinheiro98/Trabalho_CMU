import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.ui.navigation.AppTopBar
import pt.ipp.estg.trabalho_cmu.ui.navigation.DrawerUser
import pt.ipp.estg.trabalho_cmu.ui.navigation.NavGraphAdmin
import pt.ipp.estg.trabalho_cmu.ui.navigation.NavGraphPublic
import pt.ipp.estg.trabalho_cmu.ui.navigation.NavGraphUser
import pt.ipp.estg.trabalho_cmu.ui.navigation.getUserDrawerOptions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    isLoggedIn: Boolean,
    isAdmin: Boolean,onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onLogoutAndNavigate: () -> Unit = {
        // Altera o estado de autenticação
        onLogout()

        // Navega para a rota pública /home - popUpTo para limpar a back stack de rotas protegidas
        navController.navigate("home") {
            popUpTo("home") {
                inclusive = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isLoggedIn && !isAdmin,
        drawerContent = {
            if (isLoggedIn && !isAdmin) {
                DrawerUser(
                    items = getUserDrawerOptions(),
                    selected = null,
                    onSelect = {
                        scope.launch { drawerState.close() }
                        navController.navigate(it.route)
                    },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    isLoggedIn = isLoggedIn,
                    isAdmin = isAdmin,
                    currentRoute = currentRoute,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateBack = { navController.popBackStack() },
                    onLogoutClick = onLogoutAndNavigate,
                    onNotificationsClick = { navController.navigate("Notifications") },
                    onVeterinariansClick = { navController.navigate("Veterinarians") }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when {
                    !isLoggedIn -> NavGraphPublic(
                        navController = navController,
                        onLoginSuccess = onLoginSuccess
                    )
                    isAdmin -> NavGraphAdmin(navController)
                    else -> NavGraphUser(navController)
                }
            }
        }
    }
}


