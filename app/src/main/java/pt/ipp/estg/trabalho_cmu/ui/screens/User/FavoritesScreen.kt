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


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: AnimalViewModel,
    onAnimalClick: (Int) -> Unit = {}
) {
    val favorites by viewModel.favorites.observeAsState(emptyList())

    FavoritesScreenContent(
        favorites = favorites,
        onAnimalClick = onAnimalClick,
        onToggleFavorite = { viewModel.toggleFavorite(it) }
    )
}

@Composable
private fun FavoritesScreenContent(
    favorites: List<Animal>,
    onAnimalClick: (Int) -> Unit,
    onToggleFavorite: (Animal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {

        Text(
            text = "Favoritos",
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
                        isFavorite = true,
                        isLoggedIn = true,
                        onClick = { onAnimalClick(animal.id) },
                        onToggleFavorite = { onToggleFavorite(animal) }
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
            id = 1,
            firebaseUid = "mockFirebase1",
            name = "Boby",
            breed = "Labrador",
            species = "Cão",
            size = "Médio",
            birthDate = "2020-01-01",
            imageUrls = listOf(""),
            shelterFirebaseUid = "shelterMock1",
            description = "Muito amigável!"
        ),
        Animal(
            id = 2,
            firebaseUid = "mockFirebase2",
            name = "Mia",
            breed = "Siamês",
            species = "Gato",
            size = "Pequeno",
            birthDate = "2021-03-10",
            imageUrls = listOf(""),
            shelterFirebaseUid = "shelterMock1",
            description = "Adora mimos!"
        )
    )

    MaterialTheme {
        FavoritesScreenContent(
            favorites = mockFavorites,
            onAnimalClick = {},
            onToggleFavorite = {}
        )
    }
}
