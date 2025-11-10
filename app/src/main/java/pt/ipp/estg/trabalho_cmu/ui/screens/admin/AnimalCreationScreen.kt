package pt.ipp.estg.trabalho_cmu.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalCreationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val form by viewModel.animalForm.observeAsState()
    val message by viewModel.message.observeAsState()
    val error by viewModel.error.observeAsState()
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = form?.name ?: "",
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form?.breed ?: "",
            onValueChange = { viewModel.onBreedChange(it) },
            label = { Text("Raça") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form?.species ?: "",
            onValueChange = { viewModel.onSpeciesChange(it) },
            label = { Text("Espécie") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form?.size ?: "",
            onValueChange = viewModel::onSizeChange,
            label = { Text("Tamanho") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form?.birthDate ?: "",
            onValueChange = { viewModel.onBirthDateChange(it) },
            label = { Text("Data de Nascimento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form?.imageUrl ?.toString()?: "",
            onValueChange = viewModel::onImageUrlChange,
            label = { Text("Imagem") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.guardarAnimal() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Guardar", fontSize = 16.sp)
        }
    }

// 🔹 Diálogo de sucesso
    message?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            confirmButton = {
                TextButton(onClick = onNavigateBack) { Text("OK") }
            },
            title = { Text("Sucesso") },
            text = { Text(it) }
        )
    }

// 🔹 Diálogo de erro
    error?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            },
            title = { Text("Erro") },
            text = { Text(it) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalCreationPreview() {
    MaterialTheme {
        AnimalCreationScreen(onNavigateBack = {})
    }
}
