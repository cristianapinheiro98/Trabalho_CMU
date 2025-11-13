package pt.ipp.estg.trabalho_cmu.ui.navigation

import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.HomeScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.HomeScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.RegisterScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalsListGuestScreen
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.LoginScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraphPublic(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLoginSuccess: (isAdmin: Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = "Home") {
        composable("Home") {
            HomeScreen(
                onLoginClick = { navController.navigate("Login") },
                onRegisterClick = { navController.navigate("Register") },
                onGuestAnimalsClick = { navController.navigate("AnimalsListGuest") }
            )
        }
        composable("Login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { isAdmin ->
                    onLoginSuccess(isAdmin)
                },
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


        composable("AnimalsListGuest") {
            val context = LocalContext.current
            val repository = remember {
                val db = AppDatabase.getDatabase(context)
                AnimalRepository(db.animalDao())
            }

            val animalViewModel: AnimalViewModel = viewModel(
                factory = AnimalViewModelFactory(repository)
            )

            AnimalsListGuestScreen(
                viewModel = animalViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}


class AnimalViewModelFactory(
    private val repository: AnimalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnimalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

