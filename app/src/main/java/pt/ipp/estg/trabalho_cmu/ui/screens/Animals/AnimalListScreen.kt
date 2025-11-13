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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    viewModel: AnimalViewModel,
    onAnimalClick: (Int) -> Unit = {}
) {
    val favorites by viewModel.favorites.observeAsState(emptyList())
    var search by remember { mutableStateOf("") }

    val animals by viewModel.animals.observeAsState(emptyList())

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
                    IconButton(onClick = { /* abrir filtros */ }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filtrar")
                    }
                    IconButton(onClick = { /* abrir ordenação */ }) {
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

        // --- Empty state global (sem animais) ---
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
            return@Column
        }

        val filteredAnimals = animals.filter { it.name.contains(search, ignoreCase = true) }

        // --- Empty state de pesquisa (nenhum corresponde) ---
        if (filteredAnimals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum animal corresponde à pesquisa.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
            return@Column
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
                    onClick = { onAnimalClick(animal.id) },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(animal)
                    }
                )
            }
        }
    }
}

class MockAnimalViewModel : AnimalViewModel(repository = null) {

    override val animals: LiveData<List<Animal>> = MutableLiveData(
        listOf(
            Animal(1, "Leia", "Desconhecida", "Gato", "Pequeno", "2019-01-01", listOf(R.drawable.gato1), "Um gato fofo",1),
            Animal(2, "Noa", "Desconhecida", "Gato", "Pequeno", "2022-01-01", listOf(R.drawable.gato2), "Um gato fofinho",1),
            Animal(3, "Tito", "Desconhecida", "Gato", "Médio", "2011-01-01", listOf(R.drawable.gato3), "Um gato fofinho",1)
        )
    )

}


@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalListScreenPreview() {
    val mockViewModel = MockAnimalViewModel()
    MaterialTheme {
        AnimalListScreen(viewModel = mockViewModel)
    }
}
