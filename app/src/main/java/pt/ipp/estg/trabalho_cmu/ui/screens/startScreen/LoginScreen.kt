package pt.ipp.estg.trabalho_cmu.ui.screens.startScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = { viewModel.login() }, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar")
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }

    if (state.showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissDialog()
                    if (state.isSuccess) onLoginSuccess(state.isAdmin)
                }) { Text("OK") }
            },
            title = { Text(if (state.isSuccess) "Sucesso" else "Aviso") },
            text = { Text(state.dialogMessage) }
        )
    }
}
