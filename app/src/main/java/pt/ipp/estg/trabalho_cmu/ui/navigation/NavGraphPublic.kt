package pt.ipp.estg.trabalho_cmu.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.ipp.estg.trabalho_cmu.ui.screens.animals.AnimalDetailScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.animals.AnimalListScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.auth.HomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.auth.LoginScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.auth.RegisterScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.shelter.ShelterViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.user.FavoriteViewModel

/**
 * Navigation graph for the public (guest) section of the app.
 *
 * Includes:
 * - Home
 * - Login
 * - Register
 * - Guest animal catalogue
 * - Guest animal details
 *
 * Handles transitions before authentication.
 */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraphPublic(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    windowSize: WindowWidthSizeClass,
    onLoginSuccess: (isAdmin: Boolean) -> Unit
) {
    val animalViewModel: AnimalViewModel = viewModel()
    val shelterViewModel: ShelterViewModel = viewModel()

    // Guest has no favorites
    val favoriteViewModel: FavoriteViewModel? = null

    NavHost(navController = navController, startDestination = "Home") {

        composable("Home") {
            HomeScreen(
                onLoginClick = { navController.navigate("Login") },
                onRegisterClick = { navController.navigate("Register") },
                onGuestAnimalsClick = { navController.navigate("AnimalsCatalogueGuest") },
                windowSize = windowSize,
            )
        }

        composable("Login") {
            LoginScreen(
                authviewModel = authViewModel,
                onLoginSuccess = { isAdmin -> onLoginSuccess(isAdmin) },
                onNavigateBack = { navController.popBackStack() },
                windowSize = windowSize,
            )
        }

        composable("Register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("Login") {
                        popUpTo("Register") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                authviewModel = authViewModel,
                windowSize = windowSize,
            )
        }

        composable("AnimalsCatalogueGuest") {
            AnimalListScreen(
                animalViewModel = animalViewModel,
                favoriteViewModel = null,
                userId = null,
                windowSize = windowSize,
                onAnimalClick = { id -> navController.navigate("AnimalDetailGuest/$id") },
                onNavigateBack = { navController.navigate("Home") {
                    popUpTo("Home") { inclusive = true }
                } }
            )
        }

        // GUEST — animal details without adoption button
        composable(
            route = "AnimalDetailGuest/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""

            AnimalDetailScreen(
                animalId = animalId,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,
                showAdoptButton = false,
                windowSize = windowSize,
                onAdoptClick = {},
                onNavigateBack = {
                    navController.navigate("AnimalsCatalogueGuest") {
                        popUpTo("AnimalsCatalogueGuest") { inclusive = true }
                    }
                }
            )
        }
    }
}
