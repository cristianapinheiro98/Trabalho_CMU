package pt.ipp.estg.trabalho_cmu

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import pt.ipp.estg.trabalho_cmu.preferences.LanguagePreferences
import pt.ipp.estg.trabalho_cmu.utils.LocaleHelper

/**
 * Main entry point of the Pet Adoption App.
 *
 * Responsibilities:
 * - Applies the user's selected language before the activity is created,
 *   overriding [attachBaseContext] to inject the updated locale.
 * - Initializes Jetpack Compose and sets the root composable of the app.
 * - Computes the window size class (Compact, Medium, Expanded) to enable
 *   responsive UI behavior.
 * - Handles intents from notifications, specifically for walk tracking actions
 *   such as navigating to the walk screen or triggering the stop walk dialog.
 *
 * This activity is the foundation of all screens in the application, ensuring
 * that localization is applied globally and consistently.
 */
class MainActivity : ComponentActivity() {

    /**
     * Mutable state indicating whether a stop walk action was requested
     * from the notification. When true, the app should navigate to the
     * walk screen and display the stop confirmation dialog.
     */
    private var stopWalkRequested = mutableStateOf(false)

    /**
     * Mutable state indicating whether navigation to the walk screen
     * was requested, typically from tapping the ongoing walk notification.
     */
    private var navigateToWalk = mutableStateOf(false)

    /**
     * Attaches the base context with the user's preferred language applied.
     *
     * @param newBase The new base context to attach
     */
    override fun attachBaseContext(newBase: Context) {
        val lang = LanguagePreferences.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    /**
     * Called when the activity is first created.
     *
     * Initializes the Compose UI, calculates window size class for responsive
     * layouts, and processes any incoming intent (e.g., from notification actions).
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        // Handle intent from notification
        handleIntent(intent)

        setContent {
            val windowSize = calculateWindowSizeClass(this)

            // Collect the state values
            val shouldNavigateToWalk by navigateToWalk
            val shouldStopWalk by stopWalkRequested

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PetAdoptionApp(
                        windowSize = windowSize.widthSizeClass,
                        navigateToWalk = shouldNavigateToWalk,
                        stopWalkRequested = shouldStopWalk,
                        onWalkNavigationHandled = {
                            // Reset flags after navigation is handled
                            navigateToWalk.value = false
                            stopWalkRequested.value = false
                        }
                    )
                }
            }
        }
    }

    /**
     * Called when a new intent is delivered to a running activity.
     *
     * This handles the case where the activity is already running (due to
     * singleTop launch mode) and a new intent arrives from the notification.
     *
     * @param intent The new intent that was started for the activity
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Processes an incoming intent and extracts navigation flags.
     *
     * Checks for the following extras:
     * - "navigate_to_walk": If true, sets flag to navigate to walk screen
     * - "stop_walk_requested": If true, sets flag to show stop walk dialog
     *
     * @param intent The intent to process, may be null
     */
    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("navigate_to_walk", false)) {
                navigateToWalk.value = true
            }
            if (it.getBooleanExtra("stop_walk_requested", false)) {
                stopWalkRequested.value = true
            }
        }
    }
}