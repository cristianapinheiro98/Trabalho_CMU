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
/*@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    animalViewModel: AnimalViewModel,
    favoriteViewModel: FavoriteViewModel? = null,
    userId: String?,
    onAnimalClick: (String) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    Log.d(TAG, "=== INICIO AnimalListScreen ===")
    Log.d(TAG, "userId recebido: $userId")

    // Define o userId no FavoriteViewModel quando o ecrã é criado/atualizado
    LaunchedEffect(userId,favoriteViewModel) {
        if(userId!=null && favoriteViewModel!=null) {
            Log.d(TAG, "LaunchedEffect: setCurrentUser($userId)")
            favoriteViewModel?.setCurrentUser(userId)
        }else{
            Log.d(TAG, "LaunchedEffect: guest ou favoriteViewModel null, não faz setCurrentUser")
        }
    }

    // 1. Dados dos Animais (Observar do Room)
    val animals by animalViewModel.animals.observeAsState(emptyList())
    Log.d(TAG, "animals observados: ${animals.size} itens")

    val filteredAnimals by animalViewModel.filteredAnimals.observeAsState(emptyList())
    Log.d(TAG, "filteredAnimals: ${filteredAnimals.size} itens")

    // 2. Favoritos - agora observados diretamente do ViewModel
    val favorites by (favoriteViewModel?.favorites?.observeAsState(emptyList())
        ?: mutableStateOf(emptyList()))
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
            if (userId != null && favoriteViewModel != null) {
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
    Log.d("TESTE", "Room devolveu ${animals.size} animais")

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
}*/
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




