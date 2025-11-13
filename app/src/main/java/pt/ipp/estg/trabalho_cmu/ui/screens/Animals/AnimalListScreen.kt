package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalListScreen(
    viewModel: AnimalViewModel,
    onAnimalClick: (Int) -> Unit = {},
    isLoggedIn: Boolean,
) {
    val animals by viewModel.animals.observeAsState(emptyList())
    val favorites by viewModel.favorites.observeAsState(emptyList())

    AnimalListContent(
        animals = animals,
        favorites = if (isLoggedIn) favorites else emptyList(),
        isLoggedIn = isLoggedIn,
        onAnimalClick = onAnimalClick,
        onToggleFavorite = { viewModel.toggleFavorite(it) }
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListContent(
    animals: List<Animal>,
    favorites: List<Animal>,
    isLoggedIn: Boolean,
    onAnimalClick: (Int) -> Unit,
    onToggleFavorite: (Animal) -> Unit
) {
    var search by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Pesquisar") },
            placeholder = { Text("Pesquisar") },
            trailingIcon = {
                Row {
                    IconButton(onClick = { /* TODO filtros */ }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filtrar")
                    }
                    IconButton(onClick = { /* TODO ordenação */ }) {
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

        if (animals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ainda não há animais disponíveis 🐾",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
            return
        }

        val filteredAnimals = animals.filter {
            it.name.contains(search, ignoreCase = true)
        }

        if (filteredAnimals.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(
                    "Nenhum animal corresponde à pesquisa.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredAnimals) { animal ->
                AnimalCard(
                    animal = animal,
                    isFavorite = favorites.any { it.id == animal.id },
                    isLoggedIn = isLoggedIn,
                    onClick = { onAnimalClick(animal.id) },
                    onToggleFavorite = { onToggleFavorite(animal) }
                )
            }
        }
    }
}


private val previewAnimals = listOf(
    Animal(
        id = 1,
        name = "Leia",
        breed = "Desconhecida",
        species = "Gato",
        size = "Pequeno",
        birthDate = "2019-01-01",
        imageUrls = listOf(""),
        shelterId = 1,
        description = "Muito meiga!"
    ),
    Animal(
        id = 2,
        name = "Noa",
        breed = "Desconhecida",
        species = "Gato",
        size = "Pequeno",
        birthDate = "2022-01-01",
        imageUrls = listOf(""),
        shelterId = 1,
        description = "Adora colo!"
    )
)


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalListScreenPreviewLoggedIn() {
    MaterialTheme {
        AnimalListContent(
            animals = previewAnimals,
            favorites = listOf(previewAnimals.first()),
            isLoggedIn = true,
            onAnimalClick = {},
            onToggleFavorite = {}
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalListScreenPreviewGuest() {
    MaterialTheme {
        AnimalListContent(
            animals = previewAnimals,
            favorites = emptyList(),
            isLoggedIn = false,
            onAnimalClick = {},
            onToggleFavorite = {}
        )
    }
}
