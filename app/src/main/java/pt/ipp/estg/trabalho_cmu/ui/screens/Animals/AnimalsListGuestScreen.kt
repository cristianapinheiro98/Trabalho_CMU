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
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalCard
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestScreen(
    viewModel: AnimalViewModel,
    onLoginClick: () -> Unit = {}
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
                text = "Bem-vindo 🐾",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onLoginClick) {
                Text("Iniciar Sessão", color = MaterialTheme.colorScheme.primary)
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

        if (animals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ainda não há animais disponíveis 🐾",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.outline)
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
                items(animals.filter { it.name.contains(search, ignoreCase = true) }) { animal ->
                    AnimalCard(
                        animal = animal,
                        onClick = {
                            onLoginClick()
                        }
                    )
                }
            }
        }
    }
}
class Mock1 : AnimalViewModel(repository = null) {
    override val animals: LiveData<List<Animal>> = MutableLiveData(
        listOf(
            Animal(1, "Leia", "Desconhecida", "Gato", "Pequeno", "2019-01-01", listOf(R.drawable.gato1), 1),
            Animal(2, "Noa", "Desconhecida", "Gato", "Pequeno", "2022-01-01", listOf(R.drawable.gato2), 1),
            Animal(3, "Tito", "Desconhecida", "Cão", "Médio", "2018-01-01", listOf(R.drawable.dog_image), 1)
        )
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GuestScreenPreview() {
    val mockViewModel = Mock1()
    MaterialTheme {
        GuestScreen(viewModel = mockViewModel)
    }
}

