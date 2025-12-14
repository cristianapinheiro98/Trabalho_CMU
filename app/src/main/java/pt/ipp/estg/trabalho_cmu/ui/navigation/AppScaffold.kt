package pt.ipp.estg.trabalho_cmu.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterMngViewModel

/**
 * Main application scaffold responsible for:
 * - Building the top-level UI structure
 * - Handling drawer navigation for logged-in users
 * - Routing between public, admin and user navigation graphs
 * - Processing deep-link navigation from notifications (walk tracking actions)
 *
 * Behavior:
 * - Shows a navigation drawer only for logged-in non-admin users
 * - TopBar changes automatically based on login status and roles
 * - Navigation content switches depending on:
 *      • Guest mode
 *      • Admin mode
 *      • Regular user mode
 * - Handles walk-related navigation requests from notification actions
 *
 * @param isLoggedIn Whether the user is currently authenticated
 * @param isAdmin Whether the authenticated user has admin privileges
 * @param onLoginSuccess Callback invoked on successful login with admin status
 * @param onLogout Callback invoked when user logs out
 * @param windowSize Current window size class for responsive layouts
 * @param navigateToWalk Flag to trigger navigation to the active walk screen
 * @param stopWalkRequested Flag indicating the stop walk action was triggered from notification
 * @param onWalkNavigationHandled Callback to reset navigation flags after processing
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    isLoggedIn: Boolean,
    isAdmin: Boolean,
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onLogout: () -> Unit,
    windowSize: WindowWidthSizeClass,
    navigateToWalk: Boolean = false,
    stopWalkRequested: Boolean = false,
    onWalkNavigationHandled: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = viewModel()
    val shelterMngViewModel: ShelterMngViewModel = viewModel()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val onLogoutAndNavigate: () -> Unit = {
        authViewModel.logout()
        onLogout()
    }

    val userDrawerOptions = getUserDrawerOptions()
    val selectedDrawerOption = userDrawerOptions.find { it.route == currentRoute }

    // Handle navigation from notification actions
    // When user taps "Stop Walk" button in notification, navigate to walk screen
    // and trigger the stop confirmation dialog
    LaunchedEffect(navigateToWalk, stopWalkRequested) {
        if (navigateToWalk && isLoggedIn && !isAdmin) {
            // Navigate to walk screen with stop request flag if applicable
            if (stopWalkRequested) {
                navController.navigate("Walk?stopRequested=true") {
                    launchSingleTop = true
                }
            } else {
                navController.navigate("Walk") {
                    launchSingleTop = true
                }
            }
            onWalkNavigationHandled()
        }
    }

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
                    onVeterinariansClick = { navController.navigate("Veterinarians") },
                    onAdminHomeClick = { navController.navigate("AdminHome") }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when {
                    !isLoggedIn -> {
                        NavGraphPublic(
                            navController = navController,
                            authViewModel = authViewModel,
                            windowSize = windowSize,
                            onLoginSuccess = onLoginSuccess
                        )
                    }
                    isAdmin -> {
                        NavGraphAdmin(
                            navController = navController,
                            authViewModel = authViewModel,
                            windowSize = windowSize,
                        )
                    }
                    else -> {
                        NavGraphUser(
                            navController = navController,
                            windowSize = windowSize,
                            isLoggedIn = isLoggedIn
                        )
                    }
                }
            }
        }
    }
}