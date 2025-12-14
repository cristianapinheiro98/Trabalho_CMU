package pt.ipp.estg.trabalho_cmu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import pt.ipp.estg.trabalho_cmu.ui.navigation.AppScaffold

/**
 * Root composable for the Pet Adoption App.
 *
 * It provides:
 * - Application-wide MaterialTheme styling.
 * - State holders for login status and admin privileges.
 * - Injection of window size class into the app's scaffold, enabling responsive layouts.
 * - Support for deep-link navigation from notifications (walk tracking actions).
 *
 * @param windowSize The width-based window size class (Compact/Medium/Expanded)
 *                   used to adapt UI layouts depending on screen dimensions.
 * @param navigateToWalk Flag indicating if the app should navigate to the walk screen,
 *                       typically set when user taps the ongoing walk notification.
 * @param stopWalkRequested Flag indicating if the stop walk action was triggered
 *                          from the notification, which should show the confirmation dialog.
 * @param onWalkNavigationHandled Callback invoked after the walk navigation has been
 *                                processed, used to reset the navigation flags.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PetAdoptionApp(
    windowSize: WindowWidthSizeClass,
    navigateToWalk: Boolean = false,
    stopWalkRequested: Boolean = false,
    onWalkNavigationHandled: () -> Unit = {}
) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    MaterialTheme (colorScheme = MaterialTheme.colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ){
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
            windowSize = windowSize,
            navigateToWalk = navigateToWalk,
            stopWalkRequested = stopWalkRequested,
            onWalkNavigationHandled = onWalkNavigationHandled
        )
    }
}