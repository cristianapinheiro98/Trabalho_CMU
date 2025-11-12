package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val message by viewModel.message.observeAsState()
    val isAuthenticated by viewModel.isAuthenticated.observeAsState(false)

    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            val type = viewModel.tipoConta.value
            val isAdmin = type?.name?.equals("ABRIGO", ignoreCase = true) == true
            onLoginSuccess(isAdmin)
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login() },
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
                Text("Entrar")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
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
                    TextButton(onClick = { viewModel.clearMessage() }) { Text("OK") }
                },
                title = { Text("Sucesso") },
                text = { Text(it) }
            )
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateBack = {}
        )
    }
}
