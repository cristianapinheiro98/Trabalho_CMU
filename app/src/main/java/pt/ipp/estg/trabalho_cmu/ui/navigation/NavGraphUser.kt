package pt.ipp.estg.trabalho_cmu.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
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
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalDetailScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalListScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.User.FavoritesScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.User.MainOptionsScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.User.PreferencesScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Veterinarians.VeterinariansScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Walk.WalkHistoryScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Walk.WalkScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Walk.WalkSummaryScreen
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraphUser(navController: NavHostController, windowSize: WindowWidthSizeClass, isLoggedIn: Boolean = false) {
    val authViewModel: AuthViewModel = viewModel()
    val animalViewModel: AnimalViewModel = viewModel()
    val shelterViewModel: ShelterViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    //val isLoggedIn by authViewModel.isAuthenticated.observeAsState(false)

    NavHost(navController = navController, startDestination = "UserHome") {
        composable("UserHome") { Text("Menu Principal") }

        composable("Preferences") {
            PreferencesScreen()
        }

        composable("Veterinarians") {
            VeterinariansScreen()
        }

        // ========== TERMS AND CONDITIONS ==========
        composable(
            route = "TermsAndConditions/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->

            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0

            TermsAndConditionsScreen(
                onAccept = {
                    navController.navigate("OwnershipForm/$animalId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== OWNERSHIP FORM ==========
        composable(
            route = "OwnershipForm/{animalFirebaseUid}",
            arguments = listOf(navArgument("animalFirebaseUid") { type = NavType.StringType })
        ) { backStackEntry ->
            val userViewModel: UserViewModel = viewModel()
            val animalViewModel: AnimalViewModel = viewModel()
            val shelterViewModel: ShelterViewModel = viewModel()

            val animalFirebaseUid = backStackEntry.arguments?.getString("animalFirebaseUid") ?: ""
            val userFirebaseUid = authViewModel.getCurrentUserFirebaseUid() ?: ""

            OwnershipFormScreen(
                userFirebaseUid = userFirebaseUid,
                animalFirebaseUid = animalFirebaseUid,
                onSubmitSuccess = {
                    navController.navigate("ownership_confirmation/$animalFirebaseUid") {
                        popUpTo("OwnershipForm/$animalFirebaseUid") { inclusive = true }
                    }
                }
            )
        }

        // ========== OWNERSHIP CONFIRMATION ==========
        composable(
            route = "ownership_confirmation/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->

            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0

            OwnershipConfirmationScreen(
                userViewModel = userViewModel,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,
                animalId = animalId,
                onBackToHome = {
                    navController.navigate("UserHome") {
                        popUpTo("UserHome") { inclusive = true }
                    }
                }
            )
        }

        // ========== ACTIVITY SCHEDULING ==========
        composable(
            route = "ActivityScheduling/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->

            val animalId = backStackEntry.arguments?.getInt("animalId")
                ?: return@composable // cancel render if it is invalid

            val userId = authViewModel.getCurrentUserId()

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

        // ========== ACTIVITIES HISTORY ==========
        composable("ActivitiesHistory") {
            val userId = authViewModel.getCurrentUserId()

            ActivitiesHistoryScreen(
                userId = userId
            )
        }

        // ========== SOCIAL COMMUNITY ==========
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

        // ========== ANIMALS CATALOGUE ==========
        composable("AnimalsCatalogue") {
            AnimalListScreen(
                viewModel = animalViewModel,
                isLoggedIn = isLoggedIn,
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetail/$animalId")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== ANIMAL DETAIL ==========
        composable(
            route = "AnimalDetail/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0

            AnimalDetailScreen(
                animalId = animalId,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,
                showAdoptButton = isLoggedIn,
                onAdoptClick = {
                    navController.navigate("TermsAndConditions/$animalId")
                },
                onNavigateBack = {
                    navController.navigate("AnimalsCatalogue") {
                        popUpTo("AnimalsCatalogue") { inclusive = true }
                    }
                }
            )
        }

        // ========== FAVORITES ==========
        composable("Favorites") {
            FavoritesScreen(
                viewModel = animalViewModel,
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetail/$animalId")
                }
            )
        }
        // ========== USER MAIN OPTIONS ==========
        composable("UserHome") {
            MainOptionsScreen(
                navController = navController,
                hasAdoptedAnimal = true,
                windowSize = windowSize
            )
        }

        composable(
            route = "Walk/{animalId}/{animalName}",
            arguments = listOf(
                navArgument("animalId") { type = NavType.IntType },
                navArgument("animalName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0
            val animalName = backStackEntry.arguments?.getString("animalName") ?: ""

            WalkScreen(
                animalId = animalId,
                animalName = animalName,
                navController = navController
            )
        }

        composable(
            route = "WalkSummary/{animalName}",
            arguments = listOf(
                navArgument("animalName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val animalName = backStackEntry.arguments?.getString("animalName") ?: ""

            WalkSummaryScreen(
                navController = navController,
                animalName = animalName
            )
        }

        composable("WalkHistory") {
            WalkHistoryScreen()
        }
    }
}

