package pt.ipp.estg.trabalho_cmu.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
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
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.User.FavoritesScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.User.MainOptionsScreen
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.UserViewModel
import pt.ipp.estg.trabalho_cmu.R

/**
 * Navigation graph for user screens.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraphUser(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val repository = remember {
        val db = AppDatabase.getDatabase(context)
        AnimalRepository(db.animalDao())
    }
    val animalViewModel: AnimalViewModel = viewModel(
        factory = AnimalViewModelFactory(repository)
    )

    NavHost(navController = navController, startDestination = "UserHome") {
        composable("UserHome") { Text(stringResource(id = R.string.main_menu)) }
        composable("UserProfile") { Text (stringResource(id = R.string.profile)) }
        composable("Community") { Text(stringResource(id = R.string.community)) }
        composable("Veterinarians") { Text(stringResource(id = R.string.veterinaries_list)) }

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
            val animalId = 1
            val userId =  authViewModel.getCurrentUserId()

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

        composable(
            route = "ownership_confirmation/{animalId}",
            arguments = listOf(
                navArgument("animalId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userViewModel: UserViewModel = viewModel()
            val animalViewModel: AnimalViewModel = viewModel()
            val shelterViewModel: ShelterViewModel = viewModel()

            OwnershipConfirmationScreen(
                userViewModel = userViewModel,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,
                animalId = backStackEntry.arguments?.getInt("animalId") ?: 0,
                onBackToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
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
            val animalId = 1
            val userId = authViewModel.getCurrentUserId()

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
            val userId = authViewModel.getCurrentUserId()

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

        composable("AnimalsCatalogue") {
            AnimalListScreen(
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetail/$animalId")
                },
                viewModel = animalViewModel
            )
        }

        composable(
            route = "AnimalDetail/{animalId}",
            arguments = listOf(navArgument("animalId") { type = NavType.IntType })
        ) { backStackEntry ->

            val animalId = backStackEntry.arguments?.getInt("animalId") ?: 0

            val animalViewModel: AnimalViewModel = viewModel()
            val shelterViewModel: ShelterViewModel = viewModel()

            AnimalDetailScreen(
                viewModel = animalViewModel,
                animalId = animalId,
                animalViewModel = animalViewModel,
                shelterViewModel = shelterViewModel,

                onAdoptClick = {
                    navController.navigate("TermsAndConditions/$animalId")
                }
            )
        }

        composable("Favorites") {
            FavoritesScreen(
                viewModel = animalViewModel,
                onAnimalClick = { animalId ->
                    navController.navigate("AnimalDetail/$animalId")
                }
            )
        }

        composable("UserHome") {
            MainOptionsScreen(
                navController = navController,
                hasAdoptedAnimal = true
            )
        }
    }
}

