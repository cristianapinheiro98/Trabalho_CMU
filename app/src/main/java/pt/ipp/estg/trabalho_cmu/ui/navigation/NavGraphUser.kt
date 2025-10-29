package pt.ipp.estg.trabalho_cmu.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraphUser(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "UserHome") {
        composable("UserHome") { Text("Menu Principal") }
        composable("UserProfile") { Text("Página de Perfil") }
        composable("Catalogue") { Text("Catálogo de animais") }
        composable("Favourites") { Text("Animais favoritos") }
        composable("Community") { Text("Comunidade SocialTails") }
        composable("Veterinarians") { Text("Lista de Veterinários") }
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