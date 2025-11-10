package pt.ipp.estg.trabalho_cmu.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.OwnershipConfirmationScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.OwnershipFormScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.TermsAndConditionsScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.ActivitySchedulingScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Ownership.ActivitiesHistoryScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.SocialTailsComunity.SocialTailsCommunityScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.SocialTailsComunity.SocialTailsRankingScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.user.AnimalDetailScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.user.AnimalListScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.user.FavoritesScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.user.GuestScreen

/**
 * Navigation graph for user screens.
 * Now works WITHOUT Hilt - ViewModels are obtained automatically in each screen.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraphUser(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "UserHome") {
        composable("UserHome") { Text("Menu Principal") }
        composable("UserProfile") { Text("Página de Perfil") }
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
            val userId = "current_user_id" // TODO: Get from logged user

            // ViewModel is obtained automatically inside OwnershipFormScreen
            OwnershipFormScreen(
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
            val userId = "current_user_id" // TODO: Get from logged user

            // ViewModel is obtained automatically inside ActivitySchedulingScreen
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
            val userId = "current_user_id" // TODO: Get from logged user

            // ViewModel is obtained automatically inside ActivitiesHistoryScreen
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

        composable("Guest") {
            GuestScreen(
                onLoginClick = {
                    // Se fizer login, redireciona para o catálogo
                    navController.navigate("Catalogue") {
                        popUpTo("Guest") { inclusive = true }
                    }
                }
            )
        }
        composable("Catalogue") {
            AnimalListScreen(
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetail/$animalId")
                }
            )
        }
        composable(
            route = "AnimalDetail/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0
            AnimalDetailScreen(
                animalName = "Animal #$animalId",
                onAdoptClick = {
                    navController.navigate("TermsAndConditions/$animalId")
                }
            )
        }

        composable("Favourites") {
            FavoritesScreen(
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetail/$animalId")
                }
            )
        }
    }
}

