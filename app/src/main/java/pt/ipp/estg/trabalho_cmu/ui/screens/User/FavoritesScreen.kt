/*package pt.ipp.estg.trabalho_cmu.ui.screens.User

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

/**
 * UI screen displaying the list of animals favorited by the current user.
 *
 * Responsibilities:
 * - Sync favorites from Firebase upon entering the screen
 * - Observe favorites LiveData and update UI reactively
 * - Join favorite entries with the list of all animals
 * - Display favorites in a grid layout (AnimalCard components)
 * - Handle removal of favorites using the ViewModel
 * - Show snackbars for errors and confirmation messages
 *
 * Parameters:
 * @param animalViewModel Provides the list of all animals
 * @param favoriteViewModel Handles favorite operations and state
 * @param userId ID of the user whose favorites are shown
 * @param onAnimalClick Callback when an animal card is tapped
 */

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

    LaunchedEffect(userId) {
        favoriteViewModel.setCurrentUser(userId)
        favoriteViewModel.syncFavorites(userId)
    }

    val favoritesList by favoriteViewModel.favorites.observeAsState(emptyList())

    val allAnimals by animalViewModel.animals.observeAsState(emptyList())

    val favoriteAnimals = remember(favoritesList, allAnimals) {
        allAnimals.filter { animal ->
            favoritesList.any { fav -> fav.animalId == animal.id }
        }
    }

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

/**
 * Content composable for the Favorites screen.
 *
 * Displays either:
 * - A message when there are no favorites, or
 * - A grid of favorite animals using [AnimalCard] components.
 *
 * @param favorites List of Animal entities that the user has favorited.
 * @param onAnimalClick Callback when an animal card is clicked.
 * @param onRemoveFavorite Callback when the favorite toggle is used to remove an animal.
 * @param modifier Modifier applied to the root layout.
 */
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
}*/package pt.ipp.estg.trabalho_cmu.ui.screens.User

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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

/**
 * UI screen displaying the list of animals favorited by the current user.
 *
 * Responsibilities:
 * - Sync favorites from Firebase upon entering the screen
 * - Observe favorites LiveData and update UI reactively
 * - Join favorite entries with the list of all animals
 * - Display favorites in a grid layout (AnimalCard components)
 * - Handle removal of favorites using the ViewModel
 * - Show snackbars for errors and confirmation messages
 * - Adapt layout based on device size (phone/tablet)
 *
 * Parameters:
 * @param windowSize Size class of the device window
 * @param animalViewModel Provides the list of all animals
 * @param favoriteViewModel Handles favorite operations and state
 * @param userId ID of the user whose favorites are shown
 * @param onAnimalClick Callback when an animal card is tapped
 */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    windowSize: WindowWidthSizeClass,
    animalViewModel: AnimalViewModel,
    favoriteViewModel: FavoriteViewModel,
    userId: String,
    onAnimalClick: (String) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        favoriteViewModel.setCurrentUser(userId)
        favoriteViewModel.syncFavorites(userId)
    }

    val favoritesList by favoriteViewModel.favorites.observeAsState(emptyList())
    val allAnimals by animalViewModel.animals.observeAsState(emptyList())

    val favoriteAnimals = remember(favoritesList, allAnimals) {
        allAnimals.filter { animal ->
            favoritesList.any { fav -> fav.animalId == animal.id }
        }
    }

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
            windowSize = windowSize,
            favorites = favoriteAnimals,
            onAnimalClick = onAnimalClick,
            onRemoveFavorite = { animal ->
                favoriteViewModel.removeFavorite(userId, animal.id)
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Content composable for the Favorites screen.
 *
 * Displays either:
 * - A message when there are no favorites, or
 * - A grid of favorite animals using [AnimalCard] components.
 *
 * Grid adapts based on device size:
 * - Phone: 2 columns
 * - Tablet: 3-4 columns with centered content
 *
 * @param windowSize Size class of the device window
 * @param favorites List of Animal entities that the user has favorited.
 * @param onAnimalClick Callback when an animal card is clicked.
 * @param onRemoveFavorite Callback when the favorite toggle is used to remove an animal.
 * @param modifier Modifier applied to the root layout.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun FavoritesScreenContent(
    windowSize: WindowWidthSizeClass,
    favorites: List<Animal>,
    onAnimalClick: (String) -> Unit,
    onRemoveFavorite: (Animal) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTablet = windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded
    val gridColumns = when (windowSize) {
        WindowWidthSizeClass.Compact -> 2
        WindowWidthSizeClass.Medium -> 3
        WindowWidthSizeClass.Expanded -> 4
        else -> 2
    }
    val horizontalPadding = if (isTablet) 32.dp else 12.dp
    val verticalPadding = if (isTablet) 16.dp else 8.dp
    val titleFontSize = if (isTablet) 32.sp else 24.sp
    val emptyTextSize = if (isTablet) 18.sp else 16.sp
    val gridSpacing = if (isTablet) 12.dp else 8.dp
    val gridContentPadding = if (isTablet) 12.dp else 8.dp

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = if (isTablet) 1200.dp else 600.dp)
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            Text(
                text = "Meus Favoritos",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = titleFontSize
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isTablet) 16.dp else 8.dp, bottom = if (isTablet) 24.dp else 16.dp),
                textAlign = TextAlign.Center
            )

            if (favorites.isEmpty()) {
                EmptyFavoritesMessage(isTablet, emptyTextSize)
            } else {
                FavoritesGrid(
                    favorites = favorites,
                    gridColumns = gridColumns,
                    gridSpacing = gridSpacing,
                    gridContentPadding = gridContentPadding,
                    onAnimalClick = onAnimalClick,
                    onRemoveFavorite = onRemoveFavorite
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesMessage(
    isTablet: Boolean,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ainda não adicionaste nenhum animal aos favoritos.",
            color = Color.Gray,
            fontSize = textSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(if (isTablet) 48.dp else 24.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun FavoritesGrid(
    favorites: List<Animal>,
    gridColumns: Int,
    gridSpacing: androidx.compose.ui.unit.Dp,
    gridContentPadding: androidx.compose.ui.unit.Dp,
    onAnimalClick: (String) -> Unit,
    onRemoveFavorite: (Animal) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(gridContentPadding),
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        items(favorites) { animal ->
            AnimalCard(
                animal = animal,
                isFavorite = true,
                isLoggedIn = true,
                onClick = { onAnimalClick(animal.id) },
                onToggleFavorite = { onRemoveFavorite(animal) }
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Phone", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun FavoritesScreenPhonePreview() {
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
        ),
        Animal(
            id = "mock-id-2",
            name = "Luna",
            breed = "Siamês",
            species = "Gato",
            size = "Pequeno",
            birthDate = dateStringToLong("2021-05-15"),
            imageUrls = listOf(""),
            shelterId = "shelter-1",
            description = "Muito carinhosa!"
        )
    )

    MaterialTheme {
        FavoritesScreenContent(
            windowSize = WindowWidthSizeClass.Compact,
            favorites = mockFavorites,
            onAnimalClick = {},
            onRemoveFavorite = {}
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Tablet", widthDp = 900, heightDp = 1280, showBackground = true)
@Composable
private fun FavoritesScreenTabletPreview() {
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
        ),
        Animal(
            id = "mock-id-2",
            name = "Luna",
            breed = "Siamês",
            species = "Gato",
            size = "Pequeno",
            birthDate = dateStringToLong("2021-05-15"),
            imageUrls = listOf(""),
            shelterId = "shelter-1",
            description = "Muito carinhosa!"
        ),
        Animal(
            id = "mock-id-3",
            name = "Max",
            breed = "Golden Retriever",
            species = "Cão",
            size = "Grande",
            birthDate = dateStringToLong("2019-03-20"),
            imageUrls = listOf(""),
            shelterId = "shelter-1",
            description = "Muito brincalhão!"
        )
    )

    MaterialTheme {
        FavoritesScreenContent(
            windowSize = WindowWidthSizeClass.Expanded,
            favorites = mockFavorites,
            onAnimalClick = {},
            onRemoveFavorite = {}
        )
    }
}