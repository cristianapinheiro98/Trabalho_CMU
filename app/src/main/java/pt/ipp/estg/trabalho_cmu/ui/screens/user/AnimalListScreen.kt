package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    onAnimalClick: (Int) -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel,
    viewModel: AnimalViewModel = viewModel()
) {
    val animals by viewModel.animals.observeAsState(emptyList())
    val favorites by viewModel.favorites.observeAsState(emptyList())
    val isAuthenticated by authViewModel.isAuthenticated.observeAsState(false)

    if(!isAuthenticated){
        GuestScreen (onLoginClick = onNavigateToLogin)
    }
    var search by remember { mutableStateOf("") }

    Column {
        // 🔹 Barra de pesquisa
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Pesquisar") },
                placeholder = { Text("Pesquisar") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 🔹 Grelha dos animais
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
            items(animals.filter { it.name.contains(search, ignoreCase = true) }) { animal ->
                AnimalCard(
                    animal = animal,
                    isFavorite = favorites.any { it.id == animal.id },
                    onClick = { onAnimalClick(animal.id) },
                    onToggleFavorite = if (isAuthenticated) { { viewModel.toggleFavorite(animal) } } else null
                )
            }
        }
    }
}
