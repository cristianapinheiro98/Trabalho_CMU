package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun AnimalDetailScreen(
    animalId: String,
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(animalId) {
        viewModel.selecionarAnimal(animalId)
    }

    val animal = state.selectedAnimal

    animal?.let {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(it.imagemUrl),
                contentDescription = it.nome,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
            Text(it.nome, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${it.idade} anos", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(it.descricao)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { /* abrir pedido de adoção */ }) {
                Text("Quero fazer um pedido de adoção")
            }
        }
    } ?: Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Animal não encontrado.")
    }
}

