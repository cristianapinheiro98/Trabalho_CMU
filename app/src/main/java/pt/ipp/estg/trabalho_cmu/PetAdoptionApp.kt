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
import pt.ipp.estg.trabalho_cmu.ui.navigation.AppScaffold

/**
 * Root composable for the Pet Adoption App.
 *
 * It provides:
 * - Application-wide MaterialTheme styling.
 * - State holders for login status and admin privileges.
 * - Injection of window size class into the app's scaffold, enabling responsive layouts.
 *
 * Parameters:
 * @param windowSize The width-based window size class (Compact/Medium/Expanded)
 *                   used to adapt UI layouts depending on screen dimensions.
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PetAdoptionApp(windowSize: WindowWidthSizeClass) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    MaterialTheme {
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

