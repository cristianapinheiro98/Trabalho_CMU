package pt.ipp.estg.trabalho_cmu.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.ui.navigation.*
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    isLoggedIn: Boolean,
    isAdmin: Boolean,
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onLogout: () -> Unit
) {
    // ✅ Cria o AuthViewModel UMA VEZ aqui
    val authViewModel: AuthViewModel = viewModel()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    /**
     * 🔹 Ação de logout global: limpa sessão e volta à home pública
     */
    val onLogoutAndNavigate: () -> Unit = {
        authViewModel.logout()  // ✅ Chama o logout do ViewModel
        onLogout()
        navController.navigate("home") {
            popUpTo("home") { inclusive = true }
        }
    }

    val userDrawerOptions = getUserDrawerOptions()
    val selectedDrawerOption = userDrawerOptions.find { it.route == currentRoute }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isLoggedIn && !isAdmin,
        drawerContent = {
            if (isLoggedIn && !isAdmin) {
                DrawerUser(
                    items = userDrawerOptions,
                    selected = selectedDrawerOption,
                    onSelect = {
                        scope.launch { drawerState.close() }
                        navController.navigate(it.route)
                    },
                    onCloseDrawer = { scope.launch { drawerState.close() } }
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
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when {
                    !isLoggedIn -> {
                        NavGraphPublic(
                            navController = navController,
                            authViewModel = authViewModel,  // ✅ Passa o ViewModel
                            onLoginSuccess = onLoginSuccess
                        )
                    }
                    isAdmin -> {
                        NavGraphAdmin(
                            navController = navController,
                            authViewModel = authViewModel  // ✅ Passa o ViewModel
                        )
                    }
                    else -> {
                        NavGraphUser(
                            navController = navController,
                            //authViewModel = authViewModel  // ✅ Passa o ViewModel (se precisar)
                        )
                    }
                }
            }
        }
    }
}