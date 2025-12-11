package pt.ipp.estg.trabalho_cmu

import android.content.Context
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
import pt.ipp.estg.trabalho_cmu.preferences.LanguagePreferences
import pt.ipp.estg.trabalho_cmu.ui.components.NotificationPermissionHandler
import pt.ipp.estg.trabalho_cmu.ui.theme.Trabalho_CMUTheme
import pt.ipp.estg.trabalho_cmu.utils.LocaleHelper

/**
 * Main entry point of the Pet Adoption App.
 *
 * Responsibilities:
 * - Applies the user's selected language before the activity is created,
 *   overriding `attachBaseContext()` to inject the updated locale.
 * - Initializes Jetpack Compose and sets the root composable of the app.
 * - Computes the window size class (Compact, Medium, Expanded) to enable
 *   responsive UI behavior.
 *
 * This activity is the foundation of all screens in the application, ensuring
 * that localization is applied globally and consistently.
 */

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = LanguagePreferences.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        setContent {
            val windowSize = calculateWindowSizeClass(this)

            Trabalho_CMUTheme {
                NotificationPermissionHandler()

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PetAdoptionApp(
                        windowSize = windowSize.widthSizeClass
                    )
                }
            }
        }
    }
}