package pt.ipp.estg.trabalho_cmu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ╔══════════════════════════════════════════════════════════════════════════════╗
// ║                        🐾 SEEPAW SHAPES 🐾                                   ║
// ║                    Soft, Rounded & Friendly                                  ║
// ╚══════════════════════════════════════════════════════════════════════════════╝

/**
 * Shape configuration for SeePaw components.
 *
 * Design philosophy:
 * - Generously rounded corners for a soft, friendly feel
 * - Consistent with the "Cozy Paws" theme aesthetic
 * - Makes the app feel approachable and pet-friendly
 */
val Shapes = Shapes(

    // ════════════════════════════════════════════════════════════════════════
    // Extra Small - For small chips, badges, and indicators
    // ════════════════════════════════════════════════════════════════════════
    extraSmall = RoundedCornerShape(8.dp),

    // ════════════════════════════════════════════════════════════════════════
    // Small - For buttons, text fields, and small cards
    // ════════════════════════════════════════════════════════════════════════
    small = RoundedCornerShape(12.dp),

    // ════════════════════════════════════════════════════════════════════════
    // Medium - For cards, dialogs, and medium containers
    // ════════════════════════════════════════════════════════════════════════
    medium = RoundedCornerShape(16.dp),

    // ════════════════════════════════════════════════════════════════════════
    // Large - For bottom sheets, large cards, and prominent containers
    // ════════════════════════════════════════════════════════════════════════
    large = RoundedCornerShape(24.dp),

    // ════════════════════════════════════════════════════════════════════════
    // Extra Large - For full-screen dialogs and major UI sections
    // ════════════════════════════════════════════════════════════════════════
    extraLarge = RoundedCornerShape(32.dp)
)

// ════════════════════════════════════════════════════════════════════════════════
// 🎀 CUSTOM SHAPES - For special SeePaw elements
// ════════════════════════════════════════════════════════════════════════════════

/**
 * Additional shapes for SeePaw-specific UI elements.
 *
 * Example usage:
 * ```
 * Card(
 *     shape = SeePawShapes.animalCard,
 *     ...
 * )
 * ```
 */
object SeePawShapes {

    /** Fully rounded - for circular buttons and avatars */
    val circular = RoundedCornerShape(50)

    /** Pill shape - for tags and status badges */
    val pill = RoundedCornerShape(50)

    /** Animal card - extra rounded for a cozy feel */
    val animalCard = RoundedCornerShape(20.dp)

    /** Image container - slightly less rounded than card */
    val imageContainer = RoundedCornerShape(16.dp)

    /** Bottom sheet - rounded only at top */
    val bottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    /** Top app bar with rounded bottom (for special screens) */
    val roundedAppBar = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )

    /** Medal/achievement badge */
    val medalBadge = RoundedCornerShape(14.dp)

    /** Input field */
    val inputField = RoundedCornerShape(12.dp)

    /** Floating action button */
    val fab = RoundedCornerShape(18.dp)

    /** Dialog */
    val dialog = RoundedCornerShape(28.dp)

    /** Snackbar */
    val snackbar = RoundedCornerShape(12.dp)

    /** Navigation bar item indicator */
    val navIndicator = RoundedCornerShape(16.dp)

    /** Paw print shape - asymmetric for playful feel */
    val pawPrint = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 20.dp
    )
}