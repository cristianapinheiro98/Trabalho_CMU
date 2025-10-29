package pt.ipp.estg.trabalho_cmu.ui.navigation

import pt.ipp.estg.trabalho_cmu.ui.screens.StartScreen.HomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.StartScreen.LoginScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.StartScreen.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraphPublic(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "Home") {
        composable("Home") {
            HomeScreen(
                onLoginClick = { navController.navigate("Login") },
                onRegisterClick = { navController.navigate("Register") }
            )
        }
        composable("Login") {
            LoginScreen(
                onLoginSuccess = { isAdmin ->
                    // Se for admin, vai para AdminHome, senão UserHome
                    val destino = if (isAdmin) "AdminHome" else "UserHome"
                    navController.navigate(destino) {
                        popUpTo("Home") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("Register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("Login") {
                        popUpTo("Register") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
