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
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    val name by viewModel.name.observeAsState("")
    val adress by viewModel.address.observeAsState("")
    val phone by viewModel.contact.observeAsState("")
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val accountType by viewModel.accountTypeChoice.observeAsState(AccountType.USER)

    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val message by viewModel.message.observeAsState()

    RegisterScreenContent(
        name = name,
        adress = adress,
        phone = phone,
        email = email,
        password = password,
        accountType = accountType,
        isLoading = isLoading,
        error = error,
        message = message,
        onNameChange = { viewModel.name.value = it },
        onAdressChange = { viewModel.address.value = it },
        onPhoneChange = { viewModel.contact.value = it },
        onEmailChange = { viewModel.email.value = it },
        onPasswordChange = { viewModel.password.value = it },
        onAccountTypeChange = { viewModel.accountTypeChoice.value = it },
        onRegisterClick = { viewModel.register() },
        onNavigateBack = onNavigateBack,
        onRegisterSuccess = onRegisterSuccess,
        onClearError = { viewModel.clearError() },
        onClearMessage = { viewModel.clearMessage() }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(
    name: String,
    adress: String,
    phone: String,
    email: String,
    password: String,
    accountType: AccountType,
    isLoading: Boolean,
    error: String?,
    message: String?,
    onNameChange: (String) -> Unit,
    onAdressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onClearError: () -> Unit,
    onClearMessage: () -> Unit
) {

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

        // ACCOUNT TYPE DROPDOWN
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (accountType == AccountType.USER) "Utilizador" else "Abrigo",
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
                DropdownMenuItem(
                    text = { Text("Utilizador") },
                    onClick = {
                        onAccountTypeChange(AccountType.USER)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Abrigo") },
                    onClick = {
                        onAccountTypeChange(AccountType.SHELTER)
                        expanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick,
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


@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    RegisterScreenContent(
        name = "Maria",
        adress = "Rua das Flores",
        phone = "912345678",
        email = "maria@example.com",
        password = "123456",
        accountType = AccountType.USER,
        isLoading = false,
        error = null,
        message = null,
        onNameChange = {},
        onAdressChange = {},
        onPhoneChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onAccountTypeChange = {},
        onRegisterClick = {},
        onNavigateBack = {},
        onRegisterSuccess = {},
        onClearError = {},
        onClearMessage = {}
    )
}
