package pt.ipp.estg.trabalho_cmu.ui.navigation

import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.HomeScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalDetailScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalListScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.LoginScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.RegisterScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraphPublic(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLoginSuccess: (isAdmin: Boolean) -> Unit
) {
    val animalViewModel: AnimalViewModel = viewModel()
    val shelterViewModel: ShelterViewModel = viewModel()

    NavHost(navController = navController, startDestination = "Home") {

        composable("Home") {
            HomeScreen(
                onLoginClick = { navController.navigate("Login") },
                onRegisterClick = { navController.navigate("Register") },
                onGuestAnimalsClick = { navController.navigate("AnimalsCatalogueGuest") }
            )
        }

        composable("Login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { isAdmin -> onLoginSuccess(isAdmin) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("Register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("Login") {
                        popUpTo("Register") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // GUEST — animals' catalogue without favorite button
        composable("AnimalsCatalogueGuest") {
            AnimalListScreen(
                viewModel = animalViewModel,
                isLoggedIn = false,
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetailGuest/$animalId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // GUEST — animal detail page without adopt button
        composable(
            route = "AnimalDetailGuest/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->

            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0

            AnimalDetailScreen(
                animalId = animalId,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,
                showAdoptButton = false, // Guest cannot adopt
                onAdoptClick = {}
            )
        }
    }
}
