package pt.ipp.estg.trabalho_cmu.ui.screens.User

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    animalViewModel: AnimalViewModel,
    favoriteViewModel: FavoriteViewModel,
    userId: String,
    onAnimalClick: (String) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Define o userId no FavoriteViewModel quando o ecrã é criado/atualizado
    LaunchedEffect(userId) {
        favoriteViewModel.setCurrentUser(userId)
        favoriteViewModel.syncFavorites(userId)
    }

    // 1. Obter favoritos do user - agora observados diretamente do ViewModel
    val favoritesList by favoriteViewModel.favorites.observeAsState(emptyList())

    // 2. Obter todos os animais
    val allAnimals by animalViewModel.animals.observeAsState(emptyList())

    // 3. Relacionar favoritos com animais
    val favoriteAnimals = remember(favoritesList, allAnimals) {
        allAnimals.filter { animal ->
            favoritesList.any { fav -> fav.animalId == animal.id }
        }
    }

    // UI State de erros
    val uiState by favoriteViewModel.uiState.observeAsState(FavoriteUiState.Initial)

    LaunchedEffect(uiState) {
        when (uiState) {
            is FavoriteUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as FavoriteUiState.Error).message)
                favoriteViewModel.resetState()
            }
            is FavoriteUiState.FavoriteRemoved -> {
                snackbarHostState.showSnackbar("Removido dos favoritos")
                favoriteViewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        FavoritesScreenContent(
            favorites = favoriteAnimals,
            onAnimalClick = onAnimalClick,
            onRemoveFavorite = { animal ->
                favoriteViewModel.removeFavorite(userId, animal.id)
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun FavoritesScreenContent(
    favorites: List<Animal>,
    onAnimalClick: (String) -> Unit,
    onRemoveFavorite: (Animal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {

        Text(
            text = "Meus Favoritos",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ainda não adicionaste nenhum animal aos favoritos.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { animal ->
                    AnimalCard(
                        animal = animal,
                        isFavorite = true, // Sempre true nesta lista
                        isLoggedIn = true,
                        onClick = { onAnimalClick(animal.id) },
                        onToggleFavorite = { onRemoveFavorite(animal) }
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FavoritesScreenPreview() {
    val mockFavorites = listOf(
        Animal(
            id = "mock-id-1",
            name = "Boby",
            breed = "Labrador",
            species = "Cão",
            size = "Médio",
            birthDate = dateStringToLong("2020-01-01"),
            imageUrls = listOf(""),
            shelterId = "shelter-1",
            description = "Muito amigável!"
        )
    )

    MaterialTheme {
        FavoritesScreenContent(
            favorites = mockFavorites,
            onAnimalClick = {},
            onRemoveFavorite = {}
        )
    }
}