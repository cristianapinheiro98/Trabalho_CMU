package pt.ipp.estg.trabalho_cmu.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.ipp.estg.trabalho_cmu.ui.screens.ownership.OwnershipConfirmationScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.ownership.OwnershipFormScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.ownership.TermsAndConditionsScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.activity.scheduling.ActivitySchedulingScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.activity.history.ActivitiesHistoryScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.socialtailscomunity.SocialTailsCommunityScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.animals.AnimalDetailScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.animals.AnimalListScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.shelter.ShelterViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.user.FavoritesScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.user.FavoriteViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.user.MainOptionsScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.user.PreferencesScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.user.UserViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.veterinarians.VeterinariansScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.walk.history.WalkHistoryScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.walk.WalkScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.walk.summary.WalkSummaryScreen

/**
 * Navigation graph for regular logged-in users.
 *
 * Contains routes for:
 * - Home/Dashboard (MainOptionsScreen)
 * - User preferences
 * - Veterinarians list
 * - Adoption workflow (terms, form, confirmation)
 * - Activity scheduling and history
 * - SocialTails community
 * - Animal catalog and details
 * - Favorites
 * - Walk tracking, summary, and history
 *
 * @param navController Navigation controller for handling navigation
 * @param windowSize Window size class for adaptive layouts
 * @param isLoggedIn Whether user is logged in (affects certain UI elements)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraphUser(
    navController: NavHostController,
    windowSize: WindowWidthSizeClass,
    isLoggedIn: Boolean = false
) {
    // Shared ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val animalViewModel: AnimalViewModel = viewModel()
    val shelterViewModel: ShelterViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val favoriteViewModel: FavoriteViewModel = viewModel()

    NavHost(navController = navController, startDestination = "UserHome") {

        // ========== USER HOME (Dashboard) ==========
        composable("UserHome") {
            MainOptionsScreen(
                navController = navController,
                windowSize = windowSize
            )
        }

        // ========== PREFERENCES ==========
        composable("Preferences") {
            PreferencesScreen(
                userId = authViewModel.getCurrentUserFirebaseUid() ?: "",
                navController = navController
            )
        }

        // ========== VETERINARIANS ==========
        composable("Veterinarians") {
            VeterinariansScreen()
        }

        // ========== ADOPTION/OWNERSHIP WORKFLOW ==========

        // Terms and Conditions
        composable(
            route = "TermsAndConditions/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
            TermsAndConditionsScreen(
                onAccept = { navController.navigate("OwnershipForm/$animalId") },
                onNavigateBack = { navController.popBackStack() },
                windowSize = windowSize
            )
        }

        // Ownership Form
        composable(
            route = "OwnershipForm/{animalFirebaseUid}",
            arguments = listOf(navArgument("animalFirebaseUid") { type = NavType.StringType })
        ) { backStackEntry ->
            val animalFirebaseUid = backStackEntry.arguments?.getString("animalFirebaseUid") ?: ""
            val userFirebaseUid = authViewModel.getCurrentUserFirebaseUid() ?: ""

            OwnershipFormScreen(
                userFirebaseUid = userFirebaseUid,
                animalFirebaseUid = animalFirebaseUid,
                onSubmitSuccess = {
                    navController.navigate("ownership_confirmation/$animalFirebaseUid") {
                        popUpTo("OwnershipForm/$animalFirebaseUid") { inclusive = true }
                    }
                },
                windowSize = windowSize
            )
        }

        // Ownership Confirmation
        composable(
            route = "ownership_confirmation/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""

            OwnershipConfirmationScreen(
                userViewModel = userViewModel,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,
                animalId = animalId,
                onBackToHome = {
                    navController.navigate("UserHome") {
                        popUpTo("UserHome") { inclusive = true }
                    }
                },
                windowSize = windowSize
            )
        }

        // ========== ACTIVITIES ==========

        // Activity Scheduling
        composable(
            route = "ActivityScheduling/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: return@composable
            val userId = authViewModel.getCurrentUserFirebaseUid() ?: ""

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

        // Activities History
        composable("ActivitiesHistory") {
            val userId = authViewModel.getCurrentUserFirebaseUid() ?: ""
            ActivitiesHistoryScreen(userId = userId)
        }

        // ========== SOCIAL COMMUNITY ==========

        // Community Feed
        composable("SocialTailsCommunity") {
            SocialTailsCommunityScreen(
                navController = navController
            )
        }

        // ========== ANIMALS ==========

        // Animals Catalogue
        composable("AnimalsCatalogue") {
            AnimalListScreen(
                animalViewModel = animalViewModel,
                favoriteViewModel = favoriteViewModel,
                userId = authViewModel.getCurrentUserFirebaseUid(),
                windowSize = windowSize,
                onAnimalClick = { animalId -> navController.navigate("AnimalDetail/$animalId") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Animal Detail
        composable(
            route = "AnimalDetail/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""

            AnimalDetailScreen(
                animalId = animalId,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,
                showAdoptButton = isLoggedIn,
                windowSize = windowSize,
                onAdoptClick = { navController.navigate("TermsAndConditions/$animalId") },
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
                animalViewModel = animalViewModel,
                favoriteViewModel = favoriteViewModel,
                userId = authViewModel.getCurrentUserFirebaseUid() ?: "",
                onAnimalClick = { animalId -> navController.navigate("AnimalDetail/$animalId") }
            )
        }

        // ========== WALK FEATURE ==========

        // Resume Walk Screen - Used when returning from notification
        composable(
            route = "Walk?stopRequested={stopRequested}",
            arguments = listOf(
                navArgument("stopRequested") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val stopRequested = backStackEntry.arguments?.getBoolean("stopRequested") ?: false
            WalkScreen(
                navController = navController,
                animalId = null,
                stopRequested = stopRequested
            )
        }

        // Active Walk Screen - Start new walk with specific animal
        composable(
            route = "Walk/{animalId}",
            arguments = listOf(
                navArgument("animalId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
            WalkScreen(
                navController = navController,
                animalId = animalId,
                stopRequested = false
            )
        }

        // Walk Summary Screen - Displayed after completing a walk
        composable(
            route = "WalkSummary/{walkId}",
            arguments = listOf(
                navArgument("walkId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val walkId = backStackEntry.arguments?.getString("walkId") ?: ""
            WalkSummaryScreen(
                navController = navController,
                walkId = walkId
            )
        }

        // Walk History Screen - Paginated list of past walks
        composable(
            route = "WalkHistory?scrollToWalkId={scrollToWalkId}",
            arguments = listOf(
                navArgument("scrollToWalkId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val scrollToWalkId = backStackEntry.arguments?.getString("scrollToWalkId")
            WalkHistoryScreen(
                navController = navController,
                scrollToWalkId = scrollToWalkId
            )
        }
    }
}