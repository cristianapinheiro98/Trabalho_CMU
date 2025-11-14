package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    viewModel: AnimalViewModel,
    onAnimalClick: (Int) -> Unit = {},
    isLoggedIn: Boolean,
    onNavigateBack: () -> Unit
) {
    val animals by viewModel.animals.observeAsState(emptyList())
    val animalsFiltered by viewModel.animalsFiltered.observeAsState(emptyList())
    val favorites by viewModel.favorites.observeAsState(emptyList())

    val listToShow = if (animalsFiltered.isNotEmpty()) animalsFiltered else animals

    AnimalListContent(
        animals = listToShow,
        favorites = if (isLoggedIn) favorites else emptyList(),
        isLoggedIn = isLoggedIn,
        onAnimalClick = onAnimalClick,
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onSearch = { viewModel.searchByName(it) },
        onFilterSpecies = { viewModel.filterBySpecies(it) },
        onFilterSize = { viewModel.filterBySize(it) },
        onSortName = { viewModel.sortByName() },
        onSortAge = { viewModel.sortByAge() },
        onClearFilters = { viewModel.clearFilters() },
        onNavigateBack = onNavigateBack
    )
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalListContent(
    animals: List<Animal>,
    favorites: List<Animal>,
    isLoggedIn: Boolean,
    onAnimalClick: (Int) -> Unit,
    onToggleFavorite: (Animal) -> Unit,
    onSearch: (String) -> Unit,
    onFilterSpecies: (String) -> Unit,
    onFilterSize: (String) -> Unit,
    onSortName: () -> Unit,
    onSortAge: () -> Unit,
    onClearFilters: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var filterMenuOpen by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    onSearch(it)
                },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Pesquisar") },
                placeholder = { Text("Pesquisar") },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { filterMenuOpen = true }) {
                            Icon(Icons.Outlined.FilterList, contentDescription = "Filtrar")
                        }
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.Outlined.Sort, contentDescription = "Ordenar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )
        }

        // ------------ FILTER MENU ------------
        DropdownMenu(
            expanded = filterMenuOpen,
            onDismissRequest = { filterMenuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text("Cães") },
                onClick = { onFilterSpecies("Cão"); filterMenuOpen = false }
            )
            DropdownMenuItem(
                text = { Text("Gatos") },
                onClick = { onFilterSpecies("Gato"); filterMenuOpen = false }
            )
            DropdownMenuItem(
                text = { Text("Porte Pequeno") },
                onClick = { onFilterSize("Pequeno"); filterMenuOpen = false }
            )
            DropdownMenuItem(
                text = { Text("Porte Médio") },
                onClick = { onFilterSize("Médio"); filterMenuOpen = false }
            )
            DropdownMenuItem(
                text = { Text("Porte Grande") },
                onClick = { onFilterSize("Grande"); filterMenuOpen = false }
            )
            Divider()
            DropdownMenuItem(
                text = { Text("Limpar filtros") },
                onClick = { onClearFilters(); filterMenuOpen = false }
            )
        }

        // ------------ SORT MENU ------------
        DropdownMenu(
            expanded = sortMenuOpen,
            onDismissRequest = { sortMenuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text("Ordenar por nome") },
                onClick = { onSortName(); sortMenuOpen = false }
            )
            DropdownMenuItem(
                text = { Text("Ordenar por idade") },
                onClick = { onSortAge(); sortMenuOpen = false }
            )
        }

        // ------------ EMPTY LIST CHECK ------------
        if (animals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Sem animais disponíveis.")
            }
            return
        }

        // ------------ GRID LIST ------------
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animals) { animal ->
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

//user logged in
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
            onToggleFavorite = {},
            onSearch = {},
            onFilterSpecies = {},
            onFilterSize = {},
            onSortName = {},
            onSortAge = {},
            onClearFilters = {},
            onNavigateBack = {}
        )
    }
}

//user not logged in
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
            onToggleFavorite = {},
            onSearch = {},
            onFilterSpecies = {},
            onFilterSize = {},
            onSortName = {},
            onSortAge = {},
            onClearFilters = {},
            onNavigateBack = {}
        )
    }
}

//empty list
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalListScreenPreviewEmpty() {
    MaterialTheme {
        AnimalListContent(
            animals = emptyList(),
            favorites = emptyList(),
            isLoggedIn = true,
            onAnimalClick = {},
            onToggleFavorite = {},
            onSearch = {},
            onFilterSpecies = {},
            onFilterSize = {},
            onSortName = {},
            onSortAge = {},
            onClearFilters = {},
            onNavigateBack = {}
        )
    }
}

//search by name = noa
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalListScreenPreviewSearch() {
    MaterialTheme {
        AnimalListContent(
            animals = previewAnimals.filter { it.name.contains("Noa", ignoreCase = true) },
            favorites = emptyList(),
            isLoggedIn = true,
            onAnimalClick = {},
            onToggleFavorite = {},
            onSearch = {},
            onFilterSpecies = {},
            onFilterSize = {},
            onSortName = {},
            onSortAge = {},
            onClearFilters = {},
            onNavigateBack = {}
        )
    }
}


//filters by size = pequeno and species = gato
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalListScreenPreviewFiltered() {
    MaterialTheme {
        val filtered = previewAnimals.filter {
            it.species == "Gato" && it.size == "Pequeno"
        }

        AnimalListContent(
            animals = filtered,
            favorites = emptyList(),
            isLoggedIn = true,
            onAnimalClick = {},
            onToggleFavorite = {},
            onSearch = {},
            onFilterSpecies = {},
            onFilterSize = {},
            onSortName = {},
            onSortAge = {},
            onClearFilters = {},
            onNavigateBack = {}
        )
    }
}



