package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

/**
 * Home screen shown to shelters after login.
 *
 * Displays:
 * - Personalized welcome message
 * - Actions for registering animals
 * - Viewing adoption requests
 * - Responsive layout based on device size
 */

@Composable
fun ShelterHomeScreen(
    windowSize: WindowWidthSizeClass,
    authViewModel: AuthViewModel,
    onRegisterClick: () -> Unit = {},
    onRequestsClick: () -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.observeAsState()

    ShelterHomeScreenContent(
        windowSize = windowSize,
        userName = currentUser?.name ?: "",
        onRegisterClick = onRegisterClick,
        onRequestsClick = onRequestsClick
    )
}

@Composable
fun ShelterHomeScreenContent(
    windowSize: WindowWidthSizeClass,
    userName: String,
    onRegisterClick: () -> Unit,
    onRequestsClick: () -> Unit
) {
    val isTablet = windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded
    val maxWidth = if (isTablet) 700.dp else 500.dp
    val horizontalPadding = if (isTablet) 48.dp else 32.dp
    val titleSize = if (isTablet) 36.sp else 26.sp
    val bottomPadding = if (isTablet) 48.dp else 32.dp
    val buttonSpacing = if (isTablet) 24.dp else 16.dp

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeMessage(
                userName = userName,
                titleSize = titleSize,
                bottomPadding = bottomPadding,
                isTablet = isTablet
            )

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ActionButton(
                        text = stringResource(R.string.shelter_home_register_animal),
                        onClick = onRegisterClick,
                        modifier = Modifier.weight(1f),
                        isTablet = isTablet
                    )
                    ActionButton(
                        text = stringResource(R.string.shelter_home_view_requests),
                        onClick = onRequestsClick,
                        modifier = Modifier.weight(1f),
                        isTablet = isTablet
                    )
                }
            } else {
                ActionButton(
                    text = stringResource(R.string.shelter_home_register_animal),
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth(),
                    isTablet = isTablet
                )
                Spacer(modifier = Modifier.height(buttonSpacing))
                ActionButton(
                    text = stringResource(R.string.shelter_home_view_requests),
                    onClick = onRequestsClick,
                    modifier = Modifier.fillMaxWidth(),
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
private fun WelcomeMessage(
    userName: String,
    titleSize: androidx.compose.ui.unit.TextUnit,
    bottomPadding: androidx.compose.ui.unit.Dp,
    isTablet: Boolean
) {
    Text(
        text = stringResource(R.string.shelter_home_welcome, userName),
        fontSize = titleSize,
        fontWeight = FontWeight.Bold,
        textAlign = if (isTablet) TextAlign.Center else TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
    )
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
        modifier = modifier.then(
            if (isTablet) Modifier.height(56.dp) else Modifier
        )
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = if (isTablet) 18.sp else 16.sp
        )
    }
}

@Preview(name = "Phone", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun PreviewShelterHomeScreenPhone() {
    MaterialTheme {
        ShelterHomeScreenContent(
            windowSize = WindowWidthSizeClass.Compact,
            userName = "Abrigo Porto",
            onRegisterClick = {},
            onRequestsClick = {}
        )
    }
}

@Preview(name = "Tablet", widthDp = 900, heightDp = 1280, showBackground = true)
@Composable
fun PreviewShelterHomeScreenTablet() {
    MaterialTheme {
        ShelterHomeScreenContent(
            windowSize = WindowWidthSizeClass.Expanded,
            userName = "Abrigo Porto",
            onRegisterClick = {},
            onRequestsClick = {}
        )
    }
}