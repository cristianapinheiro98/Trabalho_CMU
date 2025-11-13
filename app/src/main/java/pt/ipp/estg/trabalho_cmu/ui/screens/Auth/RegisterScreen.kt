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
import pt.ipp.estg.trabalho_cmu.data.models.UserType

// ✅ Componente STATEFUL - usa ViewModel (produção)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    val name by viewModel.name.observeAsState("")
    val adress by viewModel.address.observeAsState("")
    val phone by viewModel.phone.observeAsState("")
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val userType by viewModel.userType.observeAsState(UserType.UTILIZADOR)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val message by viewModel.message.observeAsState()

    RegisterScreenContent(
        name = name,
        adress = adress,
        phone = phone,
        email = email,
        password = password,
        userType = userType,
        isLoading = isLoading,
        error = error,
        message = message,
        onNameChange = { viewModel.name.value = it },
        onAdressChange = { viewModel.address.value = it },
        onPhoneChange = { viewModel.phone.value = it },
        onEmailChange = { viewModel.email.value = it },
        onPasswordChange = { viewModel.password.value = it },
        onUserTypeChange = { viewModel.userType.value = it },
        onRegisterClick = { shelterName, shelterAddress, shelterContact ->
            if (userType == UserType.ABRIGO) {
                println("Abrigo: $shelterName - $shelterAddress - $shelterContact")
            }
            viewModel.register()
        },
        onNavigateBack = onNavigateBack,
        onRegisterSuccess = onRegisterSuccess,
        onClearError = { viewModel.clearError() },
        onClearMessage = { viewModel.clearMessage() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreenContent(
    name: String,
    adress: String,
    phone: String,
    email: String,
    password: String,
    userType: UserType,
    isLoading: Boolean,
    error: String?,
    message: String?,
    onNameChange: (String) -> Unit,
    onAdressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUserTypeChange: (UserType) -> Unit,
    onRegisterClick: (String, String, String) -> Unit,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onClearError: () -> Unit,
    onClearMessage: () -> Unit
) {
    var shelterName by remember { mutableStateOf("") }
    var shelterAddress by remember { mutableStateOf("") }
    var shelterContact by remember { mutableStateOf("") }
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
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = adress,
            onValueChange = onAdressChange,
            label = { Text("Morada") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Telefone") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = userType.label,
                onValueChange = {},
                label = { Text("Tipo de conta") },
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                UserType.entries.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(tipo.label) },
                        onClick = {
                            onUserTypeChange(tipo)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (userType == UserType.ABRIGO) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
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
                onRegisterClick(shelterName, shelterAddress, shelterContact) },
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

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
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
                TextButton(onClick = {
                    onClearMessage()
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
private fun RegisterScreenContentPreview() {
    MaterialTheme {
        RegisterScreenContent(
            name = "Maria Silva",
            adress = "Rua das Flores 123",
            phone = "912345678",
            email = "maria@example.com",
            password = "1234",
            userType = UserType.UTILIZADOR,
            isLoading = false,
            error = null,
            message = null,
            onNameChange = {},
            onAdressChange = {},
            onPhoneChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onUserTypeChange = {},
            onRegisterClick = { _, _, _ -> },
            onNavigateBack = {},
            onRegisterSuccess = {},
            onClearError = {},
            onClearMessage = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenShelterPreview() {
    MaterialTheme {
        RegisterScreenContent(
            name = "João Santos",
            adress = "Rua do Abrigo 456",
            phone = "923456789",
            email = "joao@abrigo.com",
            password = "1234",
            userType = UserType.ABRIGO,
            isLoading = false,
            error = null,
            message = null,
            onNameChange = {},
            onAdressChange = {},
            onPhoneChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onUserTypeChange = {},
            onRegisterClick = { _, _, _ -> },
            onNavigateBack = {},
            onRegisterSuccess = {},
            onClearError = {},
            onClearMessage = {}
        )
    }
}