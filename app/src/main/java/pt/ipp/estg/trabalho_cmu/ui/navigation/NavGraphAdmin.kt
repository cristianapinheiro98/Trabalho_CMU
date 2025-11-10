package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AdminHomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AnimalCreationScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AdoptionRequestScreen

@Composable
fun NavGraphAdmin(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "AdminHome") {
        composable("AdminHome") {
            AdminHomeScreen(
                onRegisterClick = { navController.navigate("AnimalCreation") },
                onRequestsClick = { navController.navigate("AdoptionRequest") }
            )
        }
        composable("AnimalCreation") {
            AnimalCreationScreen(

                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("AdoptionRequest") {
            AdoptionRequestScreen (onNavigateBack = { navController.popBackStack() })
        }
    }
}
