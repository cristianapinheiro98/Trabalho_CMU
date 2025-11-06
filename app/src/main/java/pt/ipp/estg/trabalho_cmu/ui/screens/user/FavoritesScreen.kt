package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.AuthViewModel

@Composable
fun FavoritesScreen(
    onAnimalClick: (Int) -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: AnimalViewModel = viewModel()
) {
    val favorites by viewModel.favorites.observeAsState(emptyList())
    val isAuthenticated by authViewModel.isAuthenticated.observeAsState(false)

    if (!isAuthenticated) {
        GuestScreen(onLoginClick = onNavigateToLogin)
        return
    }

    if (favorites.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum animal adicionado aos favoritos.")
        }
    } else {
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
            items(favorites) { animal ->
                AnimalCard(
                    animal = animal,
                    isFavorite = true,
                    onClick = { onAnimalClick(animal.id) },
                    onToggleFavorite = { viewModel.toggleFavorite(animal) }
                )
            }
        }
    }
}
