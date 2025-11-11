package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    onAnimalClick: (Int) -> Unit = {}
) {
    var favorites by remember { mutableStateOf(listOf<Int>()) } // IDs favoritos
    var search by remember { mutableStateOf("") }

    val animals = listOf(
        Animal(1, "Leia", "Unknown", "Cat", "Small", "2019-01-01", R.drawable.gato1, 1),
        Animal(2, "Noa", "Unknown", "Cat", "Small", "2022-01-01", R.drawable.gato2, 1),
        Animal(3, "Tito", "Unknown", "Cat", "Medium", "2011-01-01", R.drawable.gato3, 1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Barra de pesquisa
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Pesquisar") },
            placeholder = { Text("Pesquisar") },
            trailingIcon = {
                Row {
                    IconButton(onClick = { /* TODO: Filtro */ }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filtrar")
                    }
                    IconButton(onClick = { /* TODO: Ordenar */ }) {
                        Icon(Icons.Outlined.Sort, contentDescription = "Ordenar")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
        )

        // 🐾 Grelha de animais
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animals.filter { it.name.contains(search, ignoreCase = true) }) { animal ->
                AnimalCard(
                    animal = animal,
                    isFavorite = favorites.contains(animal.id),
                    onClick = { onAnimalClick(animal.id) },
                    onToggleFavorite = {
                        favorites = if (favorites.contains(animal.id)) {
                            favorites - animal.id
                        } else {
                            favorites + animal.id
                        }
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalListScreenPreview() {
    MaterialTheme {
        AnimalListScreen()
    }
}
