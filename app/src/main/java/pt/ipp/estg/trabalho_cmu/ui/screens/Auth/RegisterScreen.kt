package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.data.models.UserType
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // Observa o estado do ViewModel
    val nome by viewModel.nome.observeAsState("")
    val morada by viewModel.morada.observeAsState("")
    val telefone by viewModel.telefone.observeAsState("")
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val tipoConta by viewModel.tipoConta.observeAsState(UserType.UTILIZADOR)

    var shelterName by remember { mutableStateOf("") }
    var shelterAddress by remember { mutableStateOf("") }
    var shelterContact by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val message by viewModel.message.observeAsState()

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Criar Conta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Campos de entrada
        OutlinedTextField(
            value = nome,
            onValueChange = { viewModel.nome.value = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = morada,
            onValueChange = { viewModel.morada.value = it },
            label = { Text("Morada") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = telefone,
            onValueChange = { viewModel.telefone.value = it },
            label = { Text("Telefone") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // 🔹 Tipo de conta (dropdown)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = tipoConta.label,
                onValueChange = {},
                label = { Text("Tipo de conta") },
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                UserType.entries.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.label) },
                        onClick = {
                            viewModel.tipoConta.value = tipo
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (tipoConta == UserType.ABRIGO) {
            Divider(Modifier.padding(vertical = 8.dp))
            Text(
                "Informações do Abrigo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = shelterName,
                onValueChange = { shelterName = it },
                label = { Text("Nome do Abrigo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = shelterAddress,
                onValueChange = { shelterAddress = it },
                label = { Text("Morada do Abrigo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = shelterContact,
                onValueChange = { shelterContact = it },
                label = { Text("Contacto do Abrigo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }


        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (tipoConta == UserType.ABRIGO) {
                    println("Abrigo: $shelterName - $shelterAddress - $shelterContact")
                }

                viewModel.register()
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text("Criar Conta")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }

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

    message?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearMessage()
                    onRegisterSuccess()
                }) { Text("OK") }
            },
            title = { Text("Sucesso") },
            text = { Text(it) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            onNavigateBack = {},
            onRegisterSuccess = {}
        )
    }
}
