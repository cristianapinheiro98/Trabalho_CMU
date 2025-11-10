package pt.ipp.estg.trabalho_cmu.ui.screens.user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<Animal> = emptyList(),
    onAnimalClick: (Int) -> Unit = {},
    onToggleFavorite: ((Animal) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // 🔹 Título
        Text(
            text = "Favoritos ❤️",
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
            // 🔸 Estado vazio
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ainda não adicionaste nenhum animal aos favoritos 🐾",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            // 🐾 Lista de favoritos
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
                        isFavorite = true, // ❤️ mostra o coração
                        onClick = { onAnimalClick(animal.id) },
                        onToggleFavorite = {
                            onToggleFavorite?.invoke(animal)
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenPreview() {
    val mockFavorites = listOf(
        Animal(1, "Leia", "Unknown", "Cat", "Small", "2019-01-01", R.drawable.gato1, 1),
        Animal(2, "Noa", "Unknown", "Cat", "Small", "2022-01-01", R.drawable.gato2, 1),
        Animal(3, "Molly", "Unknown", "Cat", "Medium", "2011-01-01", R.drawable.gato4, 1)
    )

    MaterialTheme {
        FavoritesScreen(favorites = mockFavorites)
    }
}
