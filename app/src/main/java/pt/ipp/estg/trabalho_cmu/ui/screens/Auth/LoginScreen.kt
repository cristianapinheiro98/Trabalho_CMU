package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
            val isAdmin = viewModel.isAdmin()
            onLoginSuccess(isAdmin)
            viewModel.clearMessage()
        }
    }

    LoginScreenContent(
        email = email,
        password = password,
        isLoading = isLoading,
        error = error,
        message = message,
        onEmailChange = { viewModel.email.value = it },
        onPasswordChange = { viewModel.password.value = it },
        onLoginClick = { viewModel.login() },
        onNavigateBack = onNavigateBack,
        onClearError = { viewModel.clearError() },
        onClearMessage = { viewModel.clearMessage() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenContent(
    email: String,
    password: String,
    isLoading: Boolean,
    error: String?,
    message: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
    onClearMessage: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
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
        }

        error?.let {
            AlertDialog(
                onDismissRequest = onClearError,
                confirmButton = {
                    TextButton(onClick = onClearError) { Text("OK") }
                },
                title = { Text("Erro") },
                text = { Text(it) }
            )
        }

        message?.let {
            AlertDialog(
                onDismissRequest = onClearMessage,
                confirmButton = {
                    TextButton(onClick = onClearMessage) { Text("OK") }
                },
                title = { Text("Sucesso") },
                text = { Text(it) }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun LoginScreenContentPreview() {
    MaterialTheme {
        LoginScreenContent(
            email = "user@example.com",
            password = "1234",
            isLoading = false,
            error = null,
            message = null,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateBack = {},
            onClearError = {},
            onClearMessage = {}
        )
    }
}
