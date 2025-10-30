package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AdminHomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AdoptionRequest
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.AnimalCreation

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
            AnimalCreation(

                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("AdoptionRequest") {
            AdoptionRequest (onNavigateBack = { navController.popBackStack() })
        }
    }
}
