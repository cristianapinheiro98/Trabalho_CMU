package pt.ipp.estg.trabalho_cmu.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.ipp.estg.trabalho_cmu.sensors.LightSensorManager

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Main theme composable for the SeePaw app with automatic light-based theme switching.
 *
 * Features:
 * - Automatically switches between light and dark themes based on ambient light sensor
 * - Falls back to system theme if sensor is unavailable
 * - Dynamic color support for Android 12+
 *
 * How it works:
 * - Light sensor measures ambient light in lux
 * - Below 100 lux: Dark theme (dim environment)
 * - Above 100 lux: Light theme (bright environment)
 *
 * No user configuration needed - works automatically!
 *
 * @param dynamicColor Enable Material You dynamic colors on Android 12+
 * @param content The composable content to apply the theme to
 */
@Composable
fun Trabalho_CMUTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val lightSensorManager = remember { LightSensorManager(context) }

    // Observe LiveData inside Compose
    val darkThemeFromSensor by lightSensorManager.shouldUseDarkTheme
        .observeAsState(initial = false)

    DisposableEffect(Unit) {
        if (lightSensorManager.isSensorAvailable) {
            lightSensorManager.startListening()
        }
        onDispose { lightSensorManager.stopListening() }
    }

    val darkTheme = if (lightSensorManager.isSensorAvailable) {
        darkThemeFromSensor
    } else false

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
