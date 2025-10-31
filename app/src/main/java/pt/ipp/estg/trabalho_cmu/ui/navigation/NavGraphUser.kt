package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.OwnershipConfirmationScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.OwnershipFormScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.OwnershipViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.TermsAndConditionsScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.ActivitySchedulingScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.ActivitiesHistoryScreen
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
            val shelterId = "temp_shelter_123" // APAGAR: Remover quando animal estiver pronto

            val ownershipViewModel: OwnershipViewModel = hiltViewModel()

            OwnershipFormScreen(
                viewModel = ownershipViewModel,
                userId = userId,
                animalId = animalId,
                shelterId = shelterId, //APAGAR DEPOIS QUANDO TIVER O ANIMAL
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
                        popUpTo("UserHome") {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(
            route = "ActivityScheduling/{animalId}",
            arguments = listOf(
                navArgument("animalId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
            val userId = "current_user_id" // TODO: Obter do utilizador logado

            ActivitySchedulingScreen(
                userId = userId,
                animalId = animalId,
                onScheduleSuccess = {
                    navController.navigate("ActivitiesHistory") {
                        popUpTo("ActivityScheduling/$animalId") { inclusive = true }
                    }
                }
            )
        }

        composable("ActivitiesHistory") {
            val userId = "current_user_id" // TODO: Obter do utilizador logado

            ActivitiesHistoryScreen(
                userId = userId
            )
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