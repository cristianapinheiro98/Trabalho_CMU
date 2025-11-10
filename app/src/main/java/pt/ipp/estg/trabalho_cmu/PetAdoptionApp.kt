package pt.ipp.estg.trabalho_cmu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import pt.ipp.estg.trabalho_cmu.ui.screens.AppScaffold


@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
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
