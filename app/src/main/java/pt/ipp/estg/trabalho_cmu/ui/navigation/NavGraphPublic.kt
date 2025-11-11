package pt.ipp.estg.trabalho_cmu.ui.navigation

import pt.ipp.estg.trabalho_cmu.ui.screens.startScreen.HomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.LoginScreen

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
            val authviewModel: AuthViewModel = viewModel()
            RegisterScreen(
                viewModel = authviewModel,
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


