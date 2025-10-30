package pt.ipp.estg.trabalho_cmu.ui.navigation

import pt.ipp.estg.trabalho_cmu.ui.screens.startScreen.HomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.startScreen.LoginScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.startScreen.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraphPublic(
    navController: NavHostController,
    onLoginSuccess: (isAdmin: Boolean) -> Unit
) {
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
                    onLoginSuccess(isAdmin)
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


