package pt.ipp.estg.trabalho_cmu.ui.navigation

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
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.HomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.LoginScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.RegisterScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.User.FavoriteViewModel

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

    // GUEST → não há favoritos
    val favoriteViewModel: FavoriteViewModel? = null

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
                onLoginSuccess = { isAdmin -> onLoginSuccess(isAdmin) },
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
                onNavigateBack = { navController.popBackStack() },
                authviewModel = authViewModel
            )
        }

        // GUEST — catálogo de animais sem favoritos
        /*composable("AnimalsCatalogueGuest") {
            AnimalListScreen(
                animalViewModel = animalViewModel,
                favoriteViewModel = null,   // guest → null
                userId = null,             // guest
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetailGuest/$animalId")
                },
                onNavigateBack = {
                    // Voltar ao Home em vez de rota inexistente
                    navController.navigate("Home") {
                        popUpTo("Home") { inclusive = true }
                    }
                }
            )
        }*/
        composable("AnimalsCatalogueGuest") {
            AnimalListScreen(
                animalViewModel = animalViewModel,
                favoriteViewModel = null,   // << GUEST MODE
                userId = null,
                onAnimalClick = { id -> navController.navigate("AnimalDetailGuest/$id") },
                onNavigateBack = { navController.navigate("Home") {
                    popUpTo("Home") { inclusive = true }
                } }
            )
        }


        // GUEST — detalhes do animal sem botão de adoção
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
                onAdoptClick = {}
            )
        }
    }
}
