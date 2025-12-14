package pt.ipp.estg.trabalho_cmu.ui.screens.ownership

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership

/**
 * Screen responsible for handling the UI and user interaction when creating an
 * ownership (adoption) request for a specific animal. This composable screen:
 *
 * - Loads the target animal using the provided animal Firebase UID
 * - Observes UI state changes from the OwnershipViewModel
 * - Displays a form for collecting payment/account information
 * - Creates an Ownership entity and submits it to the ViewModel
 * - Shows loading indicators and snackbar messages for feedback
 *
 * Behavior:
 * - When the animal data is being loaded, a loading indicator is displayed
 * - When the ownership request is successfully created, a callback is triggered
 * - When an error occurs, a snackbar message is shown
 *
 * @param userFirebaseUid The Firebase UID of the user making the request
 * @param animalFirebaseUid The Firebase UID of the animal being adopted
 * @param onSubmitSuccess Callback executed after a successful ownership creation
 * @param modifier Optional UI modifier for layout customization
 */
@Composable
fun OwnershipFormScreen(
    userFirebaseUid: String,
    animalFirebaseUid: String,
    onSubmitSuccess: () -> Unit,
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val viewModel: OwnershipViewModel = viewModel()

    val uiState by viewModel.uiState.observeAsState(OwnershipUiState.Initial)
    val animal by viewModel.animal.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(animalFirebaseUid) {
        viewModel.loadAnimal(animalFirebaseUid)
    }

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is OwnershipUiState.OwnershipCreated -> {
                onSubmitSuccess()
                viewModel.resetState()
            }
            is OwnershipUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (animal == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2C8B7E))
        }
    } else {
        OwnershipFormContent(
            isLoading = uiState is OwnershipUiState.Loading,
            snackbarHostState = snackbarHostState,
            onSubmit = { formName, formAccount, formCvv, formCard ->
                val ownership = Ownership(
                    id = "",
                    userId = userFirebaseUid,
                    animalId = animalFirebaseUid,
                    shelterId = animal!!.shelterId
                )
                viewModel.submitOwnership(ownership)
            },
            modifier = modifier
        )
    }
}

@Composable
private fun OwnershipFormContent(
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onSubmit: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var ownerName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }

    val maxAccountDigits = 21
    val maxCardDigits = 8

    val isFormValid = accountNumber.isNotBlank() &&
            accountNumber.length <= maxAccountDigits &&
            ownerName.isNotBlank() &&
            cvv.length == 3 &&
            cardNumber.isNotBlank() &&
            cardNumber.length <= maxCardDigits

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OwnershipHeader(modifier = Modifier.padding(bottom = 16.dp))

                OwnershipFormCard(
                    accountNumber = accountNumber,
                    ownerName = ownerName,
                    cvv = cvv,
                    cardNumber = cardNumber,
                    isLoading = isLoading,
                    onAccountChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }
                        if (filtered.length <= maxAccountDigits) accountNumber = filtered
                    },
                    onNameChange = { ownerName = it },
                    onCvvChange = {
                        if (it.length <= 3 && it.all { char -> char.isDigit() }) cvv = it
                    },
                    onCardChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }
                        if (filtered.length <= maxCardDigits) cardNumber = filtered
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                OwnershipSubmitButton(
                    isLoading = isLoading,
                    isFormValid = isFormValid,
                    onClick = { onSubmit(ownerName, accountNumber, cvv, cardNumber) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Internal UI component that renders the ownership form fields and handles
 * validation logic for:
 *  - Account number
 *  - Owner name
 *  - CVV
 *  - Citizen card number
 *
 * It manages loading states, input restrictions, form validation, and submission.
 *
 * @param isLoading Indicates whether the submission process is ongoing
 * @param snackbarHostState Snackbar host used to display form errors
 * @param onSubmit Callback triggered when the form is valid and submitted
 * @param modifier Modifier for layout adjustments
 */
@Composable
private fun OwnershipFormContent(
    windowSize: WindowWidthSizeClass,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onSubmit: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var ownerName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }

    val maxAccountDigits = 21
    val maxCardDigits = 8

    val isFormValid = accountNumber.isNotBlank() &&
            accountNumber.length <= maxAccountDigits &&
            ownerName.isNotBlank() &&
            cvv.length == 3 &&
            cardNumber.isNotBlank() &&
            cardNumber.length <= maxCardDigits

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (windowSize == WindowWidthSizeClass.Compact) {
                // --- VERTICAL LAYOUT (PHONE) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OwnershipHeader(modifier = Modifier.padding(bottom = 16.dp))

                    OwnershipFormCard(
                        accountNumber = accountNumber,
                        ownerName = ownerName,
                        cvv = cvv,
                        cardNumber = cardNumber,
                        isLoading = isLoading,
                        onAccountChange = {
                            val filtered = it.filter { ch -> ch.isDigit() }
                            if (filtered.length <= maxAccountDigits) accountNumber = filtered
                        },
                        onNameChange = { ownerName = it },
                        onCvvChange = {
                            if (it.length <= 3 && it.all { char -> char.isDigit() }) cvv = it
                        },
                        onCardChange = {
                            val filtered = it.filter { ch -> ch.isDigit() }
                            if (filtered.length <= maxCardDigits) cardNumber = filtered
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OwnershipSubmitButton(
                        isLoading = isLoading,
                        isFormValid = isFormValid,
                        onClick = { onSubmit(ownerName, accountNumber, cvv, cardNumber) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                // --- HORIZONTAL LAYOUT (TABLET) ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        OwnershipHeader()

                        Spacer(modifier = Modifier.height(32.dp))

                        OwnershipSubmitButton(
                            isLoading = isLoading,
                            isFormValid = isFormValid,
                            onClick = { onSubmit(ownerName, accountNumber, cvv, cardNumber) }
                        )
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        OwnershipFormCard(
                            accountNumber = accountNumber,
                            ownerName = ownerName,
                            cvv = cvv,
                            cardNumber = cardNumber,
                            isLoading = isLoading,
                            onAccountChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }
                                if (filtered.length <= maxAccountDigits) accountNumber = filtered
                            },
                            onNameChange = { ownerName = it },
                            onCvvChange = {
                                if (it.length <= 3 && it.all { char -> char.isDigit() }) cvv = it
                            },
                            onCardChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }
                                if (filtered.length <= maxCardDigits) cardNumber = filtered
                            }
                        )
                    }
                }
            }
        }
    }
}


/**
 * Helper method to construct the header of the form.
 */
@Composable
private fun OwnershipHeader(modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = stringResource(R.string.ownership_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2C),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.ownership_subtitle),
            fontSize = 16.sp,
            color = Color(0xFF757575)
        )
    }
}

/**
 * Helper method to show the submit button.
 */
@Composable
private fun OwnershipSubmitButton(
    isLoading: Boolean,
    isFormValid: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = !isLoading && isFormValid,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF2C8B7E),
            disabledContainerColor = Color(0xFFE0E0E0),
            disabledContentColor = Color(0xFF9E9E9E)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF2C8B7E))
        } else {
            Text(
                text = stringResource(R.string.ownership_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Helper method to construct the ownership form card.
 */
@Composable
private fun OwnershipFormCard(
    accountNumber: String,
    ownerName: String,
    cvv: String,
    cardNumber: String,
    isLoading: Boolean,
    onAccountChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onCardChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB8D4D0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OwnershipTextField(
                value = accountNumber,
                onValueChange = onAccountChange,
                label = stringResource(R.string.ownership_account_number),
                enabled = !isLoading,
                leadingIcon = {
                    Text(
                        text = "PT50",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C8B7E),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            )

            OwnershipTextField(
                value = ownerName,
                onValueChange = onNameChange,
                label = stringResource(R.string.ownership_owner_name),
                enabled = !isLoading
            )

            OwnershipTextField(
                value = cvv,
                onValueChange = onCvvChange,
                label = stringResource(R.string.ownership_cvv),
                enabled = !isLoading,
                placeholder = "123"
            )

            OwnershipTextField(
                value = cardNumber,
                onValueChange = onCardChange,
                label = stringResource(R.string.ownership_citizen_card),
                enabled = !isLoading,
                placeholder = "12345678"
            )
        }
    }
}

/**
 * Custom text field wrapper used inside the ownership form.
 * It provides:
 * - A label
 * - Optional placeholder
 * - Optional leading and trailing icons
 * - Input validation rules (handled outside)
 *
 * This component ensures visual consistency across the entire form UI.
 *
 * @param value Current text value of the field
 * @param onValueChange Callback triggered when input changes
 * @param label The label displayed above the field
 * @param enabled Whether the field is interactive
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional leading icon composable
 * @param trailingIcon Optional trailing icon composable
 */
@Composable
private fun OwnershipTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C2C2C),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color(0xFF4A4A4A)
            ),
            shape = RoundedCornerShape(8.dp),
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OwnershipFormScreenPreview() {
    MaterialTheme {
        OwnershipFormContent(
            windowSize = WindowWidthSizeClass.Compact,
            isLoading = false,
            snackbarHostState = SnackbarHostState(),
            onSubmit = { _, _, _, _ -> }
        )
    }
}