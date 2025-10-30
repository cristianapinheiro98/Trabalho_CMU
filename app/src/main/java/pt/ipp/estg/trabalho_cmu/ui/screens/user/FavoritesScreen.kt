package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FavoritesScreen(
    onAnimalClick: (String) -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.favorites.isEmpty()) {
        Text(
            text = "Nenhum animal adicionado aos favoritos.",
            modifier = Modifier.padding(24.dp)
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            items(state.favorites) { animal ->
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

