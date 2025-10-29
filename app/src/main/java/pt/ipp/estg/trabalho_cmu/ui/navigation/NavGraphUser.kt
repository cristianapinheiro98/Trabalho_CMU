package pt.ipp.estg.trabalho_cmu.ui.navigation

import OwnershipRequestViewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.OwnershipConfirmationScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.OwnershipFormScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.TermsAndConditionsScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.VisitSchedulingScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.VisitsHistoryScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.SocialTailsComunity.SocialTailsCommunityScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.SocialTailsComunity.SocialTailsRankingScreen

@Composable
fun NavGraphUser(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "UserHome") {
        composable("UserHome") { Text("Menu Principal") }
        composable("UserProfile") { Text("Página de Perfil") }
        composable("Catalogue") { Text("Catálogo de animais") }
        composable("Favourites") { Text("Animais favoritos") }
        composable("Community") { Text("Comunidade SocialTails") }
        composable("Veterinarians") { Text("Lista de Veterinários") }
        composable(
            route = "TermsAndConditions/{animalId}",
            arguments = listOf(
                navArgument("animalId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""

            TermsAndConditionsScreen(
                onAccept = {
                    navController.navigate("OwnershipForm/$animalId")
                }
            )
        }

        composable(
            route = "OwnershipForm/{animalId}",
            arguments = listOf(
                navArgument("animalId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
            val userId = "current_user_id" // TODO: Obter do utilizador logado

            val ownershipViewModel: OwnershipRequestViewModel = viewModel(
                factory = OwnershipRequestViewModel.Factory(
                    ownershipRepository = TODO("Injetar repository"),
                    //animalRepository = TODO("Injetar repository")
                )
            )

            OwnershipFormScreen(
                viewModel = ownershipViewModel,
                userId = userId,
                animalId = animalId,
                onSubmitSuccess = {
                    navController.navigate("OwnershipConfirmation") {
                        popUpTo("OwnershipForm/$animalId") { inclusive = true }
                    }
                }
            )
        }

        composable("OwnershipConfirmation") {
            OwnershipConfirmationScreen(
                onBackToHome = {
                    navController.navigate("UserHome") {
                        // Limpa todas as telas do fluxo (Terms, Form, Confirmation)
                        // popUpTo remove todos os destinos até UserHome
                        popUpTo("UserHome") {
                            inclusive = true // Não inclui UserHome, pois estamos a navegar para lá
                            saveState = false
                        }
                    }
                }
            )
        }


        composable("VisitScheduling") {
            VisitSchedulingScreen()
        }

        composable("VisitsHistory") {
            VisitsHistoryScreen()
        }


        composable("SocialTailsCommunity") {
            SocialTailsCommunityScreen(
                onViewRanking = {
                    navController.navigate("SocialTailsRanking")
                }
            )
        }

        composable("SocialTailsRanking") {
            SocialTailsRankingScreen()
        }
    }
}



/*Depois de termos os ecrãs todos criados ficará assim:
*  composable("UserHome") { UserHomeScreen(onNavigate = { route -> navController.navigate(route) }) } --> porque este ecrã terá outras rotas la´dentro, que serão defininidas como on navigate
    composable("UserProfile") { UserProfileScreen() }
    composable("Catalogue") { CatalogueScreen() }
    composable("Favourites") { FavouritesScreen() }
    composable("Community") { CommunityScreen() }
    composable("Veterinarians") { VeterinariansScreen() }
    *
    * FALTAM TODOS OS OUTROS ECRÃS
}*/