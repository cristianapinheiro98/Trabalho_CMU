package pt.ipp.estg.trabalho_cmu.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterHomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.AnimalCreationScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.AdoptionRequestScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterMngViewModel

/**
 * Navigation graph for admin users.
 *
 * Includes routes:
 * - AdminHome
 * - AnimalCreation
 * - AdoptionRequest
 *
 * Provides admin-specific navigation flows.
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraphAdmin(
    navController: NavHostController,
    authViewModel: AuthViewModel
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
                authViewModel = authViewModel
            )
        }
        composable("AdoptionRequest") {
            AdoptionRequestScreen (
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() })
        }
    }
}
