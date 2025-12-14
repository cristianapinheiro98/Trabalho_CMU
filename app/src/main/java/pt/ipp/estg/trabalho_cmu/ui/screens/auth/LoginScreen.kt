package pt.ipp.estg.trabalho_cmu.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.ipp.estg.trabalho_cmu.R

/**
 * Login screen: collects credentials and forwards login logic to AuthViewModel.
 * Shows success/errors through dialogs bound to ViewModel state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authviewModel: AuthViewModel,
    onLoginSuccess: (isAdmin: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    windowSize: WindowWidthSizeClass
) {
    val isLoading by authviewModel.isLoading.observeAsState(false)
    val error by authviewModel.error.observeAsState()
    val message by authviewModel.message.observeAsState()
    val isAuthenticated by authviewModel.isAuthenticated.observeAsState(false)
    val email by authviewModel.email.observeAsState("")
    val password by authviewModel.password.observeAsState("")

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            val isAdmin = authviewModel.isAdmin()
            onLoginSuccess(isAdmin)
            authviewModel.clearMessage()
        }
    }

    LoginScreenContent(
        email = email,
        password = password,
        isLoading = isLoading,
        error = error,
        message = message,
        windowSize = windowSize,
        onEmailChange = { authviewModel.email.value = it },
        onPasswordChange = { authviewModel.password.value = it },
        onLoginClick = { authviewModel.login() },
        onNavigateBack = onNavigateBack,
        onClearError = { authviewModel.clearError() },
        onClearMessage = { authviewModel.clearMessage() }
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
    windowSize: WindowWidthSizeClass,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
    onClearMessage: () -> Unit
) {
    val isExpanded = windowSize == WindowWidthSizeClass.Expanded
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.login_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (isExpanded) {
                // --- TABLET LAYOUT (SPLIT VIEW) ---
                Row(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.login_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LoginInputs(
                                email,
                                password,
                                isLoading,
                                onEmailChange,
                                onPasswordChange,
                                onLoginClick
                            )
                        }
                    }
                }

            } else {
                // --- PHONE LAYOUT ---
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginInputs(email, password, isLoading, onEmailChange, onPasswordChange, onLoginClick)
                }
            }
        }

        // Error dialog
        error?.let {
            AlertDialog(
                onDismissRequest = onClearError,
                confirmButton = {
                    TextButton(onClick = onClearError) {
                        Text(stringResource(R.string.confirm_button_ok))
                    }
                },
                title = { Text(stringResource(R.string.error_title)) },
                text = { Text(it) }
            )
        }

        // Success dialog
        message?.let {
            AlertDialog(
                onDismissRequest = onClearMessage,
                confirmButton = {
                    TextButton(onClick = onClearMessage) {
                        Text(stringResource(R.string.confirm_button_ok))
                    }
                },
                title = { Text(stringResource(R.string.success_title)) },
                text = { Text(it) }
            )
        }
    }
}

/* Helper function to fill login inputs*/
@Composable
private fun LoginInputs(
    email: String,
    password: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password_label)) },
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
                Text(stringResource(R.string.login_button))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    MaterialTheme {
        LoginScreenContent(
            email = "",
            password = "",
            isLoading = false,
            error = null,
            message = null,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateBack = {},
            onClearError = {},
            onClearMessage = {},
            windowSize = androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact
        )
    }
}