package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterHomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.AnimalCreationScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.AdoptionRequestScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterMngViewModel

@Composable
fun NavGraphAdmin(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    shelterMngViewModel: ShelterMngViewModel
) {
    NavHost(navController = navController, startDestination = "AdminHome") {
        composable("AdminHome") {
            ShelterHomeScreen(
                authViewModel = authViewModel,
                onRegisterClick = { navController.navigate("AnimalCreation") },
                onRequestsClick = { navController.navigate("AdoptionRequest") }
            )
        }
        composable("AnimalCreation") {
            AnimalCreationScreen(
                onNavigateBack = { navController.popBackStack() },
                authViewModel = authViewModel,
                shelterMngViewModel = shelterMngViewModel,

            )
        }
        composable("AdoptionRequest") {
            AdoptionRequestScreen (
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() })
        }
    }
}
