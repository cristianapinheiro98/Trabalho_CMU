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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    animalViewModel: AnimalViewModel,
    favoriteViewModel: FavoriteViewModel = viewModel(),
    userId: String?,
    onAnimalClick: (String) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    Log.d(TAG, "=== INICIO AnimalListScreen ===")
    Log.d(TAG, "userId recebido: $userId")

    // Define o userId no FavoriteViewModel quando o ecrã é criado/atualizado
    LaunchedEffect(userId) {
        Log.d(TAG, "LaunchedEffect: setCurrentUser($userId)")
        favoriteViewModel.setCurrentUser(userId)
    }

    // 1. Dados dos Animais (Observar do Room)
    val animals by animalViewModel.animals.observeAsState(emptyList())
    Log.d(TAG, "animals observados: ${animals.size} itens")

    val filteredAnimals by animalViewModel.filteredAnimals.observeAsState(emptyList())
    Log.d(TAG, "filteredAnimals: ${filteredAnimals.size} itens")

    // 2. Favoritos - agora observados diretamente do ViewModel
    val favorites by favoriteViewModel.favorites.observeAsState(emptyList())
    Log.d(TAG, "favorites: ${favorites.size} itens")

    // 3. Decidir que lista mostrar (Filtrada ou Completa)
    val listToShow = if (filteredAnimals.isNotEmpty()) filteredAnimals else animals
    Log.d(TAG, "listToShow: ${listToShow.size} itens")

    // Proteção adicional: só renderiza quando tiver dados
    if (animals.isEmpty() && filteredAnimals.isEmpty()) {
        Log.d(TAG, "Ainda não há dados para mostrar - aguardando...")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Log.d(TAG, "A chamar AnimalListContent...")

    // Chama a UI com os dados reais
    AnimalListContent(
        animals = listToShow,
        favorites = favorites,
        isLoggedIn = userId != null,
        onAnimalClick = onAnimalClick,
        onSearch = { query ->
            Log.d(TAG, "onSearch: $query")
            animalViewModel.searchAnimals(query)
        },
        onFilterSpecies = { species ->
            Log.d(TAG, "onFilterSpecies: $species")
            animalViewModel.filterBySpecies(species)
        },
        onFilterSize = { size ->
            Log.d(TAG, "onFilterSize: $size")
            animalViewModel.filterBySize(size)
        },
        onSortName = {
            Log.d(TAG, "onSortName")
            animalViewModel.sortByName()
        },
        onSortAge = {
            Log.d(TAG, "onSortAge")
            animalViewModel.sortByAge()
        },
        onClearFilters = {
            Log.d(TAG, "onClearFilters")
            animalViewModel.clearFilters()
        },
        onNavigateBack = onNavigateBack,
        // --- Ação do ViewModel de Favoritos ---
        onToggleFavorite = { animal ->
            Log.d(TAG, "onToggleFavorite: animal=${animal.id}, userId=$userId")
            if (userId != null) {
                val isFav = favorites.any { it.animalId == animal.id }
                Log.d(TAG, "Animal ${animal.id} isFavorite: $isFav")

                if (isFav) {
                    favoriteViewModel.removeFavorite(userId, animal.id)
                } else {
                    favoriteViewModel.addFavorite(userId, animal.id)
                }
            } else {
                Log.w(TAG, "Tentativa de toggle favorite sem userId")
            }
        }
    )
}

/**
 * Conteúdo da UI (SearchBar, Filtros, Grid).
 * Mantido exatamente com as opções que tinhas.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalListContent(
    animals: List<Animal>,
    favorites: List<Favorite>, // Recebe lista de favoritos para pintar corações
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
    Log.d(TAG, "=== AnimalListContent renderizado ===")
    Log.d(TAG, "animals.size: ${animals.size}")
    Log.d(TAG, "favorites.size: ${favorites.size}")
    Log.d(TAG, "isLoggedIn: $isLoggedIn")

    var search by remember { mutableStateOf("") }
    var filterMenuOpen by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {

        // --- Top row: Back button + Search field + Filters ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = {
                Log.d(TAG, "Back button clicked")
                onNavigateBack()
            }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.go_back)
                )
            }

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    onSearch(it)
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.search_label)
                    )
                },
                placeholder = { Text(stringResource(R.string.search_label)) },
                trailingIcon = {
                    Row {
                        // Filter icon
                        Box {
                            IconButton(onClick = { filterMenuOpen = true }) {
                                Icon(
                                    Icons.Outlined.FilterList,
                                    contentDescription = stringResource(R.string.filter_label)
                                )
                            }

                            DropdownMenu(
                                expanded = filterMenuOpen,
                                onDismissRequest = { filterMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_dogs)) },
                                    onClick = { onFilterSpecies("Cão"); filterMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_cats)) },
                                    onClick = { onFilterSpecies("Gato"); filterMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_small_size)) },
                                    onClick = { onFilterSize("Pequeno"); filterMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_medium_size)) },
                                    onClick = { onFilterSize("Médio"); filterMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_large_size)) },
                                    onClick = { onFilterSize("Grande"); filterMenuOpen = false }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_clear)) },
                                    onClick = { onClearFilters(); filterMenuOpen = false }
                                )
                            }
                        }

                        // Sort icon
                        Box {
                            IconButton(onClick = { sortMenuOpen = true }) {
                                Icon(
                                    Icons.Outlined.Sort,
                                    contentDescription = stringResource(R.string.sort_label)
                                )
                            }

                            DropdownMenu(
                                expanded = sortMenuOpen,
                                onDismissRequest = { sortMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_name)) },
                                    onClick = { onSortName(); sortMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_age)) },
                                    onClick = { onSortAge(); sortMenuOpen = false }
                                )
                            }
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

        // Empty state
        if (animals.isEmpty()) {
            Log.d(TAG, "Lista vazia - mostrando empty state")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_animals_available))
            }
            return
        }

        Log.d(TAG, "A renderizar grid com ${animals.size} animais")

        // Grid of animals
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(animals) { animal ->
                // Verifica se este animal está na lista de favoritos
                val isFavorite = favorites.any { it.animalId == animal.id }
                Log.d(TAG, "Renderizando animal: ${animal.id}, isFav: $isFavorite")

                AnimalCard(
                    animal = animal,
                    isFavorite = isFavorite,
                    isLoggedIn = isLoggedIn,
                    onClick = {
                        Log.d(TAG, "Animal clicked: ${animal.id}")
                        onAnimalClick(animal.id)
                    },
                    onToggleFavorite = {
                        Log.d(TAG, "Toggle favorite clicked: ${animal.id}")
                        onToggleFavorite(animal)
                    }
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