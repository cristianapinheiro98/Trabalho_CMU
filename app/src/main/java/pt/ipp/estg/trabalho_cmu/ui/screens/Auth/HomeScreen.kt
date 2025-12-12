package pt.ipp.estg.trabalho_cmu.ui.screens.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.ipp.estg.trabalho_cmu.R

/**
 * Home screen offering three entry points:
 * - Login
 * - Register
 * - View animals without authentication
 */
@Composable
fun HomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGuestAnimalsClick: () -> Unit,
    windowSize: WindowWidthSizeClass
) {
    // If it is tablet (expanded), use row, then use column.
    val isExpanded = windowSize == WindowWidthSizeClass.Expanded


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        HomeButtonsLayout(
            isExpanded = isExpanded,
            onLoginClick = onLoginClick,
            onRegisterClick = onRegisterClick,
            onGuestAnimalsClick = onGuestAnimalsClick
        )
    }
}

/**
 * Helper function to avoid duplicate buttons code.
 * Define the buttons size, based on the value of 'isExpanded', choose the container (Row ou Column).
 */
@Composable
private fun HomeButtonsLayout(
    isExpanded: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGuestAnimalsClick: () -> Unit
) {
    val heightModifier = if (isExpanded) Modifier.height(60.dp) else Modifier.height(50.dp)

    val loginButton = @Composable { extraModifier: Modifier ->
        Button(
            onClick = onLoginClick,
            modifier = heightModifier.then(extraModifier)
        ) {
            Text(stringResource(R.string.login_button))
        }
    }

    val registerButton = @Composable { extraModifier: Modifier ->
        OutlinedButton(
            onClick = onRegisterClick,
            modifier = heightModifier.then(extraModifier)
        ) {
            Text(stringResource(R.string.register_button))
        }
    }

    val guestButton = @Composable { extraModifier: Modifier ->
        Button(
            onClick = onGuestAnimalsClick,
            modifier = heightModifier.then(extraModifier)
        ) {
            Text(stringResource(R.string.view_animals_button))
        }
    }


    if (isExpanded) {
        // --- TABLET (Row) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            loginButton(Modifier.weight(1f))
            registerButton(Modifier.weight(1f))
            guestButton(Modifier.weight(1f))
        }
    } else {
        // --- PHONE (Column) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            loginButton(Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            registerButton(Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            guestButton(Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onLoginClick = {},
            onRegisterClick = {},
            onGuestAnimalsClick = {},
            windowSize = androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact
        )
    }
}

