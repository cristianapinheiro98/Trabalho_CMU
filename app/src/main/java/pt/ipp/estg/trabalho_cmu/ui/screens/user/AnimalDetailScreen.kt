package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.AuthViewModel

@Composable
fun AnimalDetailScreen(
    animalId: Int,
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit,
    viewModel: AnimalViewModel = viewModel()
) {
    val selected by viewModel.selectedAnimal.observeAsState()
    val favorites by viewModel.favorites.observeAsState(emptyList())
    val isAuthenticated by authViewModel.isAuthenticated.observeAsState(false)

    LaunchedEffect(animalId) { viewModel.selecionarAnimal(animalId) }

    val animal = selected ?: return Box(
        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) { Text("Animal não encontrado.") }

    val isFavorite = favorites.any { it.id == animal.id }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box {
            Image(
                painter = painterResource(animal.imageUrl),
                contentDescription = animal.name,
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )

            if (!isAuthenticated) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar sessão para adotar")
                }
            } else {
                Button(
                    onClick = { /* pedido adoção */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quero fazer um pedido de adoção")
                }
            }

        }

        Column(Modifier.padding(16.dp)) {
            Text(animal.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Idade: ${animal.birthDate} anos")
            Spacer(Modifier.height(16.dp))
            Text(
                "Olá! Sou ${animal.name}. Adoro brincar, dormir ao sol e ronronar quando me fazem festas. 🐾",
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { /* pedido adoção */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quero fazer um pedido de adoção")
            }
        }
    }
}
