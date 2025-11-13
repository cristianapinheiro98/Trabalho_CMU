package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalsListGuestScreen(
    viewModel: AnimalViewModel,
    onNavigateBack: () -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val animals by viewModel.animals.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bem-vindo",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onNavigateBack) {
                Text("Regressar ao Menu Principal", color = MaterialTheme.colorScheme.primary)
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Pesquisar") },
            placeholder = { Text("Pesquisar") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        val filteredAnimals =
            animals.filter { it.name.contains(search, ignoreCase = true) }

        if (animals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ainda não há animais disponíveis.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
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
                items(filteredAnimals) { animal ->
                    AnimalCard(
                        animal = animal,
                        onClick = { onNavigateBack() }
                    )
                }
            }
        }
    }
}


class MockAnimalGuestViewModel : AnimalViewModel(repository = null) {
    override val animals: LiveData<List<Animal>> = MutableLiveData(
        listOf(
            Animal(
                id = 1,
                name = "Leia",
                breed = "Siamês",
                species = "Gato",
                size = "Pequeno",
                birthDate = "2019-01-01",
                imageUrls = listOf("https://placekitten.com/400/300"),
                shelterId = 1
            ),
            Animal(
                id = 2,
                name = "Noa",
                breed = "Persa",
                species = "Gato",
                size = "Pequeno",
                birthDate = "2022-01-01",
                imageUrls = listOf("https://placekitten.com/430/320"),
                shelterId = 1
            ),
            Animal(
                id = 3,
                name = "Tito",
                breed = "Labrador",
                species = "Cão",
                size = "Médio",
                birthDate = "2018-01-01",
                imageUrls = listOf("https://placedog.net/500/400"),
                shelterId = 1
            )
        )
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GuestScreenPreview() {
    val mockViewModel = MockAnimalGuestViewModel()
    MaterialTheme {
        AnimalsListGuestScreen(
            viewModel = mockViewModel,
            onNavigateBack = {}
        )
    }
}
