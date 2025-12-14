package pt.ipp.estg.trabalho_cmu.ui.screens.ownership

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R

/**
 * Screen responsible for displaying the application's Terms and Conditions to the user.
 * It presents:
 *  - A scrollable text block containing the terms
 *  - A checkbox that users must tick to confirm their acceptance
 *  - A "Continue" button enabled only when the checkbox is selected
 *  - A "Back" button to navigate to the previous screen
 *
 * Behavior:
 * - If the user tries to continue without accepting the terms, a snackbar warning appears.
 * - Once accepted, the `onAccept` callback is triggered.
 *
 * @param onAccept Callback executed when the user accepts the terms and proceeds
 * @param modifier Optional Modifier for layout customization
 * @param onNavigateBack Callback executed when the “Back” button is pressed
 */
@Composable
fun TermsAndConditionsScreen(
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    windowSize: WindowWidthSizeClass
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val warningMessage = stringResource(R.string.terms_warning_message)
    var termsAccepted by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (windowSize == WindowWidthSizeClass.Compact) {
                // --- VERTICAL LAYOUT (Phone) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TermsHeader()

                    TermsContent()

                    Spacer(modifier = Modifier.height(24.dp))

                    TermsControlSection(
                        termsAccepted = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        onContinueClick = {
                            if (termsAccepted) {
                                onAccept()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(warningMessage)
                                }
                            }
                        },
                        onBackClick = onNavigateBack
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                // --- HORIZONTAL LAYOUT HORIZONTAL (Tablet) ---
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .verticalScroll(scrollState)
                    ) {
                        TermsHeader(textAlign = TextAlign.Start)
                        TermsContent()
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TermsControlSection(
                            termsAccepted = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            onContinueClick = {
                                if (termsAccepted) {
                                    onAccept()
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(warningMessage)
                                    }
                                }
                            },
                            onBackClick = onNavigateBack
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper method to construct the terms header.
 */
@Composable
private fun TermsHeader(
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.terms_title),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2C2C2C),
        textAlign = textAlign,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

/**
 * Helper method to construct the terms content.
 */
@Composable
private fun TermsContent(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.terms_content),
                fontSize = 14.sp,
                color = Color(0xFF2C2C2C),
                lineHeight = 20.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

/**
 * Helper method to construct the terms control section and continue button.
 */
@Composable
private fun TermsControlSection(
    termsAccepted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4A4A4A),
                    uncheckedColor = Color(0xFF9E9E9E),
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.terms_accept_checkbox),
                fontSize = 16.sp,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A4A4A),
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.continue_button),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF4A4A4A)
            )
        ) {
            Text(
                text = stringResource(R.string.back_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermsAndConditionsPreview() {
    TermsAndConditionsScreen(
        onAccept = { },
        windowSize = WindowWidthSizeClass.Compact
    )
}