package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import android.annotation.SuppressLint
import android.app.Application
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.data.models.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val name by viewModel.name.observeAsState("")
    val adress by viewModel.address.observeAsState("")
    val phone by viewModel.contact.observeAsState("")
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val userType by viewModel.userType.observeAsState(UserType.UTILIZADOR)

    val shelterName by viewModel.shelterName.observeAsState("")
    val shelterAddress by viewModel.shelterAddress.observeAsState("")
    val shelterContact by viewModel.shelterContact.observeAsState("")

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
            value = name,
            onValueChange = { viewModel.name.value = it },
            label = { Text("Nome completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = adress,
            onValueChange = { viewModel.address.value = it },
            label = { Text("Morada") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { viewModel.contact.value = it },
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

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = userType.label,
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
                            viewModel.userType.value = tipo
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (userType == UserType.ABRIGO) {
            Divider(Modifier.padding(vertical = 8.dp))
            Text(
                "Informações do Abrigo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = shelterName,
                onValueChange = { viewModel.shelterName.value = it },
                label = { Text("Nome do Abrigo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = shelterAddress,
                onValueChange = { viewModel.shelterAddress.value = it },
                label = { Text("Morada do Abrigo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = shelterContact,
                onValueChange = { viewModel.shelterContact.value = it },
                label = { Text("Contacto do Abrigo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }


        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
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
            onDismissRequest = { viewModel.clearMessage()},
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


@SuppressLint("ViewModelConstructorInComposable")
class MockAuthPreviewViewModel :
    AuthViewModel(Application()) {

    init {
        name.value = "Maria Silva"
        address.value = "Rua das Flores 123"
        contact.value = "912345678"
        email.value = "maria@example.com"
        password.value = "Password1!"
        userType.value = UserType.ABRIGO

        shelterName.value = "Abrigo Porto Feliz"
        shelterAddress.value = "Rua dos Animais 321"
        shelterContact.value = "222333444"
    }

    // Evitar chamadas reais a BD:
    override fun register() {
        message.value = "Conta criada (preview)"
    }
}



@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    val mockViewModel = remember {  MockAuthPreviewViewModel()}

    MaterialTheme {
        RegisterScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onRegisterSuccess = {}
        )
    }
}


