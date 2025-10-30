package pt.ipp.estg.trabalho_cmu.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AnimalCreation(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val form = state.animalForm



    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = form.nome,
            onValueChange = viewModel::onNomeChange,
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form.raca,
            onValueChange = viewModel::onRacaChange,
            label = { Text("Raça") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form.cor,
            onValueChange = viewModel::onCorChange,
            label = { Text("Cor") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = form.dataNascimento,
            onValueChange = viewModel::onDataNascimentoChange,
            label = { Text("Data de Nascimento") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color(0xFF37474F)
                )
            }
            Text(
                "Registar Animal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.guardarAnimal() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Guardar", fontSize = 16.sp)
        }

        state.dialogMessage?.let {
            AlertDialog(
                onDismissRequest = { viewModel.fecharDialogo() },
                confirmButton = {
                    TextButton(onClick = onNavigateBack) { Text("OK") }
                },
                title = { Text(if (state.isSuccessDialog) "Sucesso" else "Aviso") },
                text = { Text(it) }
            )
        }
    }
}

