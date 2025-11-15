package pt.ipp.estg.trabalho_cmu.ui.theme

import pt.ipp.estg.trabalho_cmu.ui.theme.*
import pt.ipp.estg.trabalho_cmu.ui.theme.Purple80
import pt.ipp.estg.trabalho_cmu.ui.theme.PurpleGrey80
import pt.ipp.estg.trabalho_cmu.ui.theme.Pink80
import pt.ipp.estg.trabalho_cmu.ui.theme.Purple40
import pt.ipp.estg.trabalho_cmu.ui.theme.PurpleGrey40
import pt.ipp.estg.trabalho_cmu.ui.theme.Pink40
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Defines the dark color scheme for the application's theme.
 * These colors are used when the app is in dark mode and dynamic color is disabled.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Defines the light color scheme for the application's theme.
 * These colors are used when the app is in light mode and dynamic color is disabled.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * The main theme composable for the "Trabalho_CMU" application.
 *
 * This function applies the appropriate color scheme and typography to its content.
 * It supports both light and dark themes and enables dynamic coloring on Android 12+
 * if available and enabled.
 *
 * @param darkTheme A boolean that determines whether to use the dark color scheme.
 *                  Defaults to the system's current setting ([isSystemInDarkTheme]).
 * @param dynamicColor A boolean that enables the use of user-generated wallpaper colors
 *                     on Android 12 (API 31) and higher. Defaults to `true`.
 * @param content The composable content to which this theme will be applied.
 */
@Composable
fun Trabalho_CMUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}