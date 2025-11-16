package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authviewModel: AuthViewModel
) {
    val name by authviewModel.name.observeAsState("")
    val address by authviewModel.address.observeAsState("")
    val phone by authviewModel.phone.observeAsState("")
    val email by authviewModel.email.observeAsState("")
    val password by authviewModel.password.observeAsState("")
    val accountType by authviewModel.accountTypeChoice.observeAsState(AccountType.USER)

    val isLoading by authviewModel.isLoading.observeAsState(false)
    val error by authviewModel.error.observeAsState()
    val message by authviewModel.message.observeAsState()

    RegisterScreenContent(
        name = name,
        address = address,
        phone = phone,
        email = email,
        password = password,
        accountType = accountType,
        isLoading = isLoading,
        error = error,
        message = message,
        onNameChange = { authviewModel.name.value = it },
        onAddressChange = { authviewModel.address.value = it },
        onPhoneChange = {
            val filtered = it.filter(Char::isDigit).take(9)
            authviewModel.phone.value = filtered
        },
        onEmailChange = { authviewModel.email.value = it },
        onPasswordChange = { authviewModel.password.value = it },
        onAccountTypeChange = { authviewModel.accountTypeChoice.value = it },
        onRegisterClick = { authviewModel.register() },
        onNavigateBack = onNavigateBack,
        onRegisterSuccess = onRegisterSuccess,
        onClearError = { authviewModel.clearError() },
        onClearMessage = { authviewModel.clearMessage() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(
    name: String,
    address: String,
    phone: String,
    email: String,
    password: String,
    accountType: AccountType,
    isLoading: Boolean,
    error: String?,
    message: String?,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text(stringResource(R.string.address_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(R.string.phone_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password_label)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value =
                        if (accountType == AccountType.USER)
                            stringResource(R.string.user_label)
                        else
                            stringResource(R.string.shelter_label),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.account_type_label)) },
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
                        text = { Text(stringResource(R.string.user_label)) },
                        onClick = {
                            onAccountTypeChange(AccountType.USER)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.shelter_label)) },
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
                    Text(stringResource(R.string.register_button))
                }
            }
        }

        error?.let {
            AlertDialog(
                onDismissRequest = onClearError,
                confirmButton = {
                    TextButton(onClick = onClearError) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
                title = { Text(stringResource(R.string.error_title)) },
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
                    }) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
                title = { Text(stringResource(R.string.success_title)) },
                text = { Text(it) }
            )
        }
    }
}

@Preview(showBackground = true)
    @Composable
    fun RegisterPreview() {
        RegisterScreenContent(
            name = "Maria",
            address = "Rua das Flores",
            phone = "912345678",
            email = "maria@example.com",
            password = "123456",
            accountType = AccountType.USER,
            isLoading = false,
            error = null,
            message = null,
            onNameChange = {},
            onAddressChange = {},
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

