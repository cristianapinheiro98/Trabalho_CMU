package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
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
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard
import pt.ipp.estg.trabalho_cmu.ui.screens.User.FavoriteViewModel
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong

private const val TAG = "AnimalListScreen"

/**
 * Ecrã principal do Catálogo.
 * Liga o ViewModel à UI e gere os estados dos favoritos.
 */
@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    animalViewModel: AnimalViewModel,
    favoriteViewModel: FavoriteViewModel? = null,   // null = guest
    userId: String?,
    onAnimalClick: (String) -> Unit = {},
    onNavigateBack: () -> Unit
) {

    // ================================
    //        OBSERVAR OS DADOS
    // ================================
    val animals by animalViewModel.animals.observeAsState(emptyList())
    val filteredAnimals by animalViewModel.filteredAnimals.observeAsState(emptyList())

    // Apenas users têm favoritos
    val favorites by (favoriteViewModel?.favorites?.observeAsState(emptyList())
        ?: mutableStateOf(emptyList()))

    val listToShow = if (filteredAnimals.isNotEmpty()) filteredAnimals else animals

    // ================================
    //  SE ESTIVER VAZIO → LOADING
    // ================================
    if (animals.isEmpty() && filteredAnimals.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    // ================================
    //            UI PRINCIPAL
    // ================================
    AnimalListContent(
        animals = listToShow,
        favorites = favorites,
        isLoggedIn = userId != null,
        onAnimalClick = onAnimalClick,
        onSearch = { animalViewModel.searchAnimals(it) },
        onFilterSpecies = { animalViewModel.filterBySpecies(it) },
        onFilterSize = { animalViewModel.filterBySize(it) },
        onSortName = { animalViewModel.sortByName() },
        onSortAge = { animalViewModel.sortByAge() },
        onClearFilters = { animalViewModel.clearFilters() },
        onNavigateBack = onNavigateBack,
        onToggleFavorite = { animal ->
            if (userId != null && favoriteViewModel != null) {
                val isFav = favorites.any { it.animalId == animal.id }
                if (isFav) favoriteViewModel.removeFavorite(userId, animal.id)
                else favoriteViewModel.addFavorite(userId, animal.id)
            }
        }
    )
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalListContent(
    animals: List<Animal>,
    favorites: List<Favorite>,
    isLoggedIn: Boolean,
    onAnimalClick: (String) -> Unit,
    onSearch: (String) -> Unit,
    onFilterSpecies: (String) -> Unit,
    onFilterSize: (String) -> Unit,
    onSortName: () -> Unit,
    onSortAge: () -> Unit,
    onClearFilters: () -> Unit,
    onNavigateBack: () -> Unit,
    onToggleFavorite: (Animal) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var filterMenu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ) {

        // -----------------------------------------
        // TOP BAR (BACK + SEARCH + FILTERS)
        // -----------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    onSearch(it)
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                placeholder = { Text("Pesquisar") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                trailingIcon = {
                    Row {
                        Box {
                            IconButton(onClick = { filterMenu = true }) {
                                Icon(Icons.Outlined.FilterList, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = filterMenu,
                                onDismissRequest = { filterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Cães") },
                                    onClick = { onFilterSpecies("Cão"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Gatos") },
                                    onClick = { onFilterSpecies("Gato"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Pequeno") },
                                    onClick = { onFilterSize("Pequeno"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Médio") },
                                    onClick = { onFilterSize("Médio"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Grande") },
                                    onClick = { onFilterSize("Grande"); filterMenu = false }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Limpar filtros") },
                                    onClick = { onClearFilters(); filterMenu = false }
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { sortMenu = true }) {
                                Icon(Icons.Outlined.Sort, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = sortMenu,
                                onDismissRequest = { sortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nome (A-Z)") },
                                    onClick = { onSortName(); sortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Idade") },
                                    onClick = { onSortAge(); sortMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // -----------------------------------------
        //          LISTA DE ANIMAIS
        // -----------------------------------------
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animals) { animal ->
                val isFav = favorites.any { it.animalId == animal.id }

                AnimalCard(
                    animal = animal,
                    isFavorite = isFav,
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
        id = "preview-1",
        name = "Leia",
        breed = "Desconhecida",
        species = "Gato",
        size = "Pequeno",
        birthDate = dateStringToLong("2019-01-01"),
        imageUrls = listOf(""),
        shelterId = "shelter-1",
        description = "Muito meiga!"
    ),
    Animal(
        id = "preview-2",
        name = "Noa",
        breed = "Desconhecida",
        species = "Gato",
        size = "Pequeno",
        birthDate = dateStringToLong("2022-01-01"),
        imageUrls = listOf(""),
        shelterId = "shelter-1",
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
            favorites = emptyList(),
            isLoggedIn = true,
            onAnimalClick = {},
            onSearch = {},
            onFilterSpecies = {},
            onFilterSize = {},
            onSortName = {},
            onSortAge = {},
            onClearFilters = {},
            onNavigateBack = {},
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
            onSearch = {},
            onFilterSpecies = {},
            onFilterSize = {},
            onSortName = {},
            onSortAge = {},
            onClearFilters = {},
            onNavigateBack = {},
            onToggleFavorite = {}
        )
    }
}




