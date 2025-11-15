package pt.ipp.estg.trabalho_cmu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import pt.ipp.estg.trabalho_cmu.ui.screens.AppScaffold


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PetAdoptionApp(windowSize: WindowWidthSizeClass) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    MaterialTheme {
        // Sends the status and the callbacks to the AppScaffold
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
            },
            windowSize = windowSize
        )
    }
}

