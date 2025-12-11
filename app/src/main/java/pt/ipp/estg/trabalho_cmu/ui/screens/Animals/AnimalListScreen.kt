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
import androidx.compose.ui.res.stringResource
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
 * Screen that displays a list of animals. Provides:
 *
 * - A search bar for filtering animals by name
 * - A loading indicator while data is fetched
 * - Automatic UI updates based on ViewModel state
 *
 * @param animalViewModel ViewModel used to fetch and observe animal data.
 * @param onAnimalClick Callback triggered when an animal card is tapped.
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

    LaunchedEffect(userId) {
        if (userId != null && favoriteViewModel != null) {
            Log.d(TAG, "Definindo userId no FavoriteViewModel: $userId")
            favoriteViewModel.setCurrentUser(userId)
            favoriteViewModel.syncFavorites(userId)
        }
    }

    val animals by animalViewModel.animals.observeAsState(emptyList())
    val filteredAnimals by animalViewModel.filteredAnimals.observeAsState(emptyList())

    // Only users have favorites
    val favorites by (favoriteViewModel?.favorites?.observeAsState(emptyList())
        ?: mutableStateOf(emptyList()))

    val listToShow = if (filteredAnimals.isNotEmpty()) filteredAnimals else animals

    if (animals.isEmpty() && filteredAnimals.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_animals_available))
        }
        return
    }

    AnimalListContent(
        animals = listToShow,
        favorites = favorites,
        isLoggedIn = userId != null,
        onAnimalClick = onAnimalClick,
        onSearch = { animalViewModel.searchAnimals(it) },
        onFilterSpecies = { animalViewModel.filterBySpecies(it) },
        onFilterSize = { animalViewModel.filterBySize(it) },
        onSortNameAsc = { animalViewModel.sortByNameAsc() },
        onSortNameDesc = { animalViewModel.sortByNameDesc() },
        onSortAgeAsc = { animalViewModel.sortByAgeAsc() },
        onSortAgeDesc = { animalViewModel.sortByAgeDesc() },
        onClearFilters = { animalViewModel.clearFilters() },
        onNavigateBack = onNavigateBack,
        onToggleFavorite = { animal ->
            Log.d("FAV_DEBUG", "Clique no coração do animal ${animal.id}, userId=$userId, vm=${favoriteViewModel != null}")
            if (userId != null && favoriteViewModel != null) {
                val isFav = favorites.any { it.animalId == animal.id }
                if (isFav) {
                    favoriteViewModel.removeFavorite(userId, animal.id)

                } else{
                    favoriteViewModel.addFavorite(userId, animal.id)

                }

            }else{
                Log.d("FAV_DEBUG", "Não executou toggle: userId ou favoriteViewModel null")
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
    onSortNameAsc: () -> Unit,
    onSortNameDesc: () -> Unit,
    onSortAgeAsc: () -> Unit,
    onSortAgeDesc: () -> Unit,
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
        // TOP BAR (BACK + SEARCH + FILTERS + SORT)
        // -----------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
            }

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    onSearch(it)
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                placeholder = { R.string.search_label },
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
                                    text = { Text(stringResource(R.string.filter_dogs)) },
                                    onClick = { onFilterSpecies("Dog"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_cats)) },
                                    onClick = { onFilterSpecies("Cat"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_small_size)) },
                                    onClick = { onFilterSize("Small"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_medium_size)) },
                                    onClick = { onFilterSize("Medium"); filterMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_large_size)) },
                                    onClick = { onFilterSize("Large"); filterMenu = false }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_clear)) },
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
                                    text = { Text(stringResource(R.string.sort_by_name_asc)) },
                                    onClick = { onSortNameAsc(); sortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_name_desc)) },
                                    onClick = { onSortNameDesc(); sortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_age_asc)) },
                                    onClick = { onSortAgeAsc(); sortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_age_desc)) },
                                    onClick = { onSortAgeDesc(); sortMenu = false }
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        //Grid of Animals
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animals,key = { it.id } ) { animal ->
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
            onSortNameAsc = {},
            onSortNameDesc = {},
            onSortAgeAsc = {},
            onSortAgeDesc = {},
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
            onSortNameAsc = {},
            onSortNameDesc = {},
            onSortAgeAsc = {},
            onSortAgeDesc = {},
            onClearFilters = {},
            onNavigateBack = {},
            onToggleFavorite = {}
        )
    }
}