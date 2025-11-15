package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.OwnershipViewModel

/**
 * Screen responsible for collecting all required fields to create an Ownership request.
 *
 * Notes:
 * - UI strings come from string resources whenever available.
 * - Backend and validation errors remain hardcoded in English inside the ViewModel.
 * - The layout and logic remain identical to the original user-provided file.
 */
@Composable
fun OwnershipFormScreen(
    userId: Int,
    animalId: Int,
    onSubmitSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: OwnershipViewModel = viewModel()

    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val submissionSuccess by viewModel.submissionSuccess.observeAsState(false)

    // Load animal details to obtain shelterId
    LaunchedEffect(animalId) {
        viewModel.loadAnimalDetails(animalId)
    }

    // Navigate after successful submission
    LaunchedEffect(submissionSuccess) {
        if (submissionSuccess) {
            delay(500)
            viewModel.resetSubmissionSuccess()
            onSubmitSuccess()
        }
    }

    val animal by viewModel.animal.observeAsState()

    animal?.let { animalData ->
        OwnershipFormContent(
            isLoading = isLoading,
            error = error,
            onSubmit = { request ->
                viewModel.submitOwnership(request)
            },
            userId = userId,
            animalId = animalId,
            shelterId = animalData.shelterId,
            modifier = modifier
        )
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF2C8B7E)
            )
        }
    }
}

/**
 * Internal UI content used by the Ownership Form Screen.
 * Contains all fields and the final submit button.
 */
@Composable
private fun OwnershipFormContent(
    isLoading: Boolean,
    error: String?,
    onSubmit: (Ownership) -> Unit,
    userId: Int,
    animalId: Int,
    shelterId: Int,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var accountNumber by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
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

    // Show error message (already hardcoded in English inside the ViewModel)
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB8D4D0)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OwnershipTextField(
                            value = accountNumber,
                            onValueChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }
                                if (filtered.length <= maxAccountDigits) accountNumber = filtered
                            },
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
                            onValueChange = { ownerName = it },
                            label = stringResource(R.string.ownership_owner_name),
                            enabled = !isLoading
                        )

                        OwnershipTextField(
                            value = cvv,
                            onValueChange = {
                                if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                                    cvv = it
                                }
                            },
                            label = stringResource(R.string.ownership_cvv),
                            enabled = !isLoading,
                            placeholder = stringResource(R.string.cvv_placeholder)
                        )

                        OwnershipTextField(
                            value = cardNumber,
                            onValueChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }
                                if (filtered.length <= maxCardDigits) cardNumber = filtered
                            },
                            label = stringResource(R.string.ownership_card_passport),
                            enabled = !isLoading,
                            placeholder = stringResource(R.string.card_number_placeholder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isFormValid) {
                            val ownership = Ownership(
                                id = 0,
                                userId = userId,
                                animalId = animalId,
                                shelterId = shelterId,
                                ownerName = ownerName,
                                accountNumber = "PT50$accountNumber",
                                cvv = cvv,
                                cardNumber = cardNumber,
                                status = OwnershipStatus.PENDING,
                                createdAt = System.currentTimeMillis()
                            )
                            onSubmit(ownership)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(48.dp),
                    enabled = !isLoading && isFormValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2C8B7E),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color(0xFF9E9E9E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF2C8B7E)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.ownership_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
            isLoading = false,
            error = null,
            onSubmit = { },
            userId = 1,
            animalId = 1,
            shelterId = 1
        )
    }
}
