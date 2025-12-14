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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import pt.ipp.estg.trabalho_cmu.sensors.LightSensorManager

// ╔══════════════════════════════════════════════════════════════════════════════╗
// ║                     🐾 SEEPAW THEME CONFIGURATION 🐾                         ║
// ║                        Cozy Paws - Light & Dark                              ║
// ╚══════════════════════════════════════════════════════════════════════════════╝

/**
 * Light Color Scheme - "Sunny Day Walk"
 * Warm, inviting, and cheerful pastel tones
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = PeachyPaw,
    onPrimary = ChocolateFur,
    primaryContainer = PeachyPawContainer,
    onPrimaryContainer = ChocolateFur,

    // Secondary colors
    secondary = LavenderWhiskers,
    onSecondary = ChocolateFur,
    secondaryContainer = LavenderContainer,
    onSecondaryContainer = ChocolateFur,

    // Tertiary colors
    tertiary = MintTail,
    onTertiary = ChocolateFur,
    tertiaryContainer = MintContainer,
    onTertiaryContainer = ChocolateFur,

    // Background & Surface
    background = CreamyFur,
    onBackground = ChocolateFur,
    surface = SoftCloud,
    onSurface = ChocolateFur,
    surfaceVariant = WarmMist,
    onSurfaceVariant = CoffeeBeans,

    // Inverse colors
    inverseSurface = MidnightDen,
    inverseOnSurface = CreamText,
    inversePrimary = PeachyPawLight,

    // Error colors
    error = WorriedPup,
    onError = FluffyWhite,
    errorContainer = WorriedPupLight,
    onErrorContainer = ChocolateFur,

    // Outline & Dividers
    outline = SoftOutline,
    outlineVariant = Color(0xFFD8CCC8),

    // Scrim for modals
    scrim = Color(0x52000000),

    // Surface tint
    surfaceTint = PeachyPaw
)

/**
 * Dark Color Scheme - "Cozy Night Den"
 * Warm dark tones that are easy on the eyes
 * Not pure black - maintains the cozy feel
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = PeachyPawLight,
    onPrimary = PeachyPawContainerDark,
    primaryContainer = PeachyPawDark,
    onPrimaryContainer = PeachyPawContainer,

    // Secondary colors
    secondary = LavenderWhiskersLight,
    onSecondary = LavenderContainerDark,
    secondaryContainer = LavenderWhiskersDark,
    onSecondaryContainer = LavenderContainer,

    // Tertiary colors
    tertiary = MintTailLight,
    onTertiary = MintContainerDark,
    tertiaryContainer = MintTailDark,
    onTertiaryContainer = MintContainer,

    // Background & Surface
    background = MidnightDen,
    onBackground = CreamText,
    surface = CozyNight,
    onSurface = CreamText,
    surfaceVariant = DreamyShadow,
    onSurfaceVariant = SoftCreamText,

    // Inverse colors
    inverseSurface = CreamyFur,
    inverseOnSurface = ChocolateFur,
    inversePrimary = PeachyPaw,

    // Error colors
    error = WorriedPupLight,
    onError = Color(0xFF5F1412),
    errorContainer = WorriedPupDark,
    onErrorContainer = WorriedPupLight,

    // Outline & Dividers
    outline = DarkOutline,
    outlineVariant = Color(0xFF524B5B),

    // Scrim for modals
    scrim = Color(0x52000000),

    // Surface tint
    surfaceTint = PeachyPawLight
)

/**
 * Main theme composable for the SeePaw app with automatic light-based theme switching.
 *
 * Features:
 * - 🌸 Beautiful pastel color palette (Cozy Paws theme)
 * - 🌙 Automatic dark/light mode based on ambient light sensor
 * - 📱 Dynamic color support for Android 12+ (Material You)
 * - 🎨 Warm, inviting tones perfect for a pet adoption app
 *
 * How the light sensor works:
 * - Below 100 lux: Dark theme (cozy evening mode)
 * - Above 100 lux: Light theme (sunny day mode)
 *
 * @param dynamicColor Enable Material You dynamic colors on Android 12+
 *                     Set to false to always use the Cozy Paws palette
 * @param content The composable content to apply the theme to
 */
@Composable
fun Trabalho_CMUTheme(
    dynamicColor: Boolean = false, // Default false to show our beautiful palette!
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

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
        // Dynamic colors (Material You) - only if enabled and Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Our beautiful Cozy Paws palette
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// ════════════════════════════════════════════════════════════════════════════════
// 🎨 THEME EXTENSION COLORS
// ════════════════════════════════════════════════════════════════════════════════
// Use these for elements not covered by Material color scheme

/**
 * Extended color palette for SeePaw-specific UI elements.
 * Access these directly when you need colors outside the Material spec.
 *
 * Example usage:
 * ```
 * Box(modifier = Modifier.background(SeePawColors.success))
 * Icon(tint = SeePawColors.favorite)
 * ```
 */
object SeePawColors {
    // Status colors
    val success = HappyTail
    val successLight = HappyTailLight
    val warning = SunnyBelly
    val warningLight = SunnyBellyLight
    val info = SkyNose
    val infoLight = SkyNoseLight

    // Pet status
    val available = AvailablePet
    val pending = PendingAdoption
    val adopted = AdoptedLove

    // Interactive
    val favorite = HeartBeat
    val highlight = Sparkle
    val pawPrint = PawPrint

    // Trophies & Medals
    val gold = GoldenBone
    val silver = SilverCollar
    val bronze = BronzePaw

    // Achievement badges
    val achievementPurple = SuperWalker
    val achievementBlue = LoyalFriend
    val achievementOrange = EarlyBird
    val achievementDark = NightOwl

    // Gradients (for special elements)
    val gradientSunset = SunsetPaws
    val gradientMorning = MorningWalk
    val gradientCelebration = CelebrationGradient
    val gradientGolden = GoldenHour
    val gradientRainbow = RainbowPride
}