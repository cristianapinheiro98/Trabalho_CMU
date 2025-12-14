package pt.ipp.estg.trabalho_cmu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

// ╔══════════════════════════════════════════════════════════════════════════════╗
// ║                      🐾 SEEPAW TYPOGRAPHY 🐾                                 ║
// ║                    Modern, Friendly & Readable                               ║
// ╚══════════════════════════════════════════════════════════════════════════════╝

/**
 * Custom font family for SeePaw.
 *
 * To use custom fonts, add the font files to res/font/ and uncomment below:
 *
 * Example with Poppins (recommended for this app):
 * 1. Download Poppins from Google Fonts
 * 2. Add files to res/font/
 * 3. Uncomment the PoppinsFamily below
 */

// Uncomment if you add Poppins font files:
/*
val PoppinsFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_light, FontWeight.Light)
)
*/

// Using default font family for now (works great too!)
val AppFontFamily = FontFamily.Default

/**
 * SeePaw Typography Configuration
 *
 * A friendly, modern typography scale that's:
 * - Easy to read (good line heights and letter spacing)
 * - Warm and inviting (rounded appearance where possible)
 * - Accessible (minimum 14sp for body text)
 * - Consistent with Material 3 guidelines
 */
val Typography = Typography(

    // ════════════════════════════════════════════════════════════════════════
    // 📰 DISPLAY STYLES - For hero sections and big headers
    // ════════════════════════════════════════════════════════════════════════

    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),

    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),

    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // ════════════════════════════════════════════════════════════════════════
    // 📝 HEADLINE STYLES - For screen titles and sections
    // ════════════════════════════════════════════════════════════════════════

    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // ════════════════════════════════════════════════════════════════════════
    // 🏷️ TITLE STYLES - For cards, dialogs, and list items
    // ════════════════════════════════════════════════════════════════════════

    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ════════════════════════════════════════════════════════════════════════
    // 📖 BODY STYLES - For main content and descriptions
    // ════════════════════════════════════════════════════════════════════════

    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ════════════════════════════════════════════════════════════════════════
    // 🏷️ LABEL STYLES - For buttons, chips, and small UI elements
    // ════════════════════════════════════════════════════════════════════════

    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ════════════════════════════════════════════════════════════════════════════════
// 🎀 CUSTOM TEXT STYLES - For special SeePaw elements
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Additional text styles for SeePaw-specific UI elements.
 *
 * Example usage:
 * ```
 * Text(
 *     text = "🎉 Parabéns!",
 *     style = SeePawTextStyles.celebration
 * )
 * ```
 */
object SeePawTextStyles {

    /** For celebration dialogs and success messages */
    val celebration = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

    /** For animal names on cards */
    val animalName = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    /** For pet stats (age, weight, etc.) */
    val petStat = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    /** For medal/achievement labels */
    val medalLabel = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    /** For walk distance/time display */
    val walkStat = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    /** For timestamps and metadata */
    val timestamp = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp
    )

    /** For empty state messages */
    val emptyState = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

    /** For notification titles */
    val notificationTitle = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

    /** For button text with emphasis */
    val buttonEmphasis = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
}