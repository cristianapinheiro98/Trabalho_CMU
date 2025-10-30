package pt.ipp.estg.trabalho_cmu

import AppScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun PetAdoptionApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    MaterialTheme {
        // Passa o estado e callbacks para o AppScaffold
        AppScaffold(
            isLoggedIn = isLoggedIn,
            isAdmin = isAdmin,
            onLoginSuccess = { admin ->
                isLoggedIn = true
                isAdmin = admin
            },
            onLogout = {
                isLoggedIn = false
                isAdmin = false
            }
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewUserApp() {
    MaterialTheme {
        AppScaffold(
            isLoggedIn = true,
            isAdmin = false,
            onLoginSuccess = {},
            onLogout = {}
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewAdminApp() {
    MaterialTheme {
        AppScaffold(
            isLoggedIn = true,
            isAdmin = true,
            onLoginSuccess = {},
            onLogout = {}
        )
    }
}
