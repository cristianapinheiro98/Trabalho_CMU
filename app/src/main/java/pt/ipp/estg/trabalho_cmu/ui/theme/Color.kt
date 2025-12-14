package pt.ipp.estg.trabalho_cmu.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ╔══════════════════════════════════════════════════════════════════════════════╗
// ║                        🐾 SEEPAW COLOR PALETTE 🐾                            ║
// ║                     Cozy Paws Theme - Pastel Edition                         ║
// ╚══════════════════════════════════════════════════════════════════════════════╝

// ════════════════════════════════════════════════════════════════════════════════
// 🌸 PRIMARY - "Peachy Paw" (Warm Coral/Peach tones)
// ════════════════════════════════════════════════════════════════════════════════
val PeachyPaw = Color(0xFFFFB5A7)           // Main primary - soft coral
val PeachyPawLight = Color(0xFFFFD6CC)      // Lighter variant
val PeachyPawDark = Color(0xFFE8968A)       // Darker variant for contrast
val PeachyPawContainer = Color(0xFFFFF0ED)  // Very light for containers (light mode)
val PeachyPawContainerDark = Color(0xFF5C3D38) // Container for dark mode

// ════════════════════════════════════════════════════════════════════════════════
// 💜 SECONDARY - "Lavender Whiskers" (Soft Purple/Lavender)
// ════════════════════════════════════════════════════════════════════════════════
val LavenderWhiskers = Color(0xFFB8A9C9)        // Main secondary
val LavenderWhiskersLight = Color(0xFFD4C8E0)   // Lighter variant
val LavenderWhiskersDark = Color(0xFF9485A8)    // Darker variant
val LavenderContainer = Color(0xFFF5F0FA)       // Light container
val LavenderContainerDark = Color(0xFF4A4154)   // Dark container

// ════════════════════════════════════════════════════════════════════════════════
// 🌿 TERTIARY - "Mint Tail" (Fresh Mint Green)
// ════════════════════════════════════════════════════════════════════════════════
val MintTail = Color(0xFFA8E6CF)            // Main tertiary - fresh mint
val MintTailLight = Color(0xFFC5F0DD)       // Lighter variant
val MintTailDark = Color(0xFF7BC4A8)        // Darker variant
val MintContainer = Color(0xFFECFBF4)       // Light container
val MintContainerDark = Color(0xFF2D4A3E)   // Dark container

// ════════════════════════════════════════════════════════════════════════════════
// 🌙 BACKGROUNDS & SURFACES
// ════════════════════════════════════════════════════════════════════════════════
// Light Mode
val CreamyFur = Color(0xFFFFFBF8)            // Main background - warm cream
val SoftCloud = Color(0xFFFFF8F5)            // Surface - slightly warmer
val FluffyWhite = Color(0xFFFFFFFF)          // Pure white for cards
val WarmMist = Color(0xFFF5EDE8)             // Surface variant

// Dark Mode - "Cozy Night" palette (not pure black, warm dark tones)
val MidnightDen = Color(0xFF1A1A2E)          // Main background - deep blue-purple
val CozyNight = Color(0xFF25253D)            // Surface - slightly lighter
val DreamyShadow = Color(0xFF2D2D47)         // Surface variant
val StarryNight = Color(0xFF363654)          // Elevated surface

// ════════════════════════════════════════════════════════════════════════════════
// 📝 TEXT COLORS
// ════════════════════════════════════════════════════════════════════════════════
// Light Mode
val ChocolateFur = Color(0xFF3D2C29)         // Primary text - warm dark brown
val CoffeeBeans = Color(0xFF5D4A47)          // Secondary text
val DustyPaws = Color(0xFF8B7672)            // Tertiary/hint text

// Dark Mode
val CreamText = Color(0xFFFFF5F0)            // Primary text
val SoftCreamText = Color(0xFFE8DDD8)        // Secondary text
val MutedCreamText = Color(0xFFB5A9A5)       // Tertiary/hint text

// ════════════════════════════════════════════════════════════════════════════════
// ✨ ACCENT & SPECIAL COLORS
// ════════════════════════════════════════════════════════════════════════════════
// Success - "Happy Tail"
val HappyTail = Color(0xFF7BD389)            // Success green
val HappyTailLight = Color(0xFFB5EABD)
val HappyTailDark = Color(0xFF4CAF5C)

// Error - "Worried Pup"
val WorriedPup = Color(0xFFFF8A80)           // Soft error red
val WorriedPupLight = Color(0xFFFFBCB5)
val WorriedPupDark = Color(0xFFE57373)

// Warning - "Sunny Belly"
val SunnyBelly = Color(0xFFFFD180)           // Warm warning orange
val SunnyBellyLight = Color(0xFFFFE4B5)
val SunnyBellyDark = Color(0xFFFFAB40)

// Info - "Sky Nose"
val SkyNose = Color(0xFF90CAF9)              // Soft info blue
val SkyNoseLight = Color(0xFFBBDEFB)
val SkyNoseDark = Color(0xFF64B5F6)

// ════════════════════════════════════════════════════════════════════════════════
// 🏆 GAMIFICATION - Medals & Achievements
// ════════════════════════════════════════════════════════════════════════════════
// Trophies
val GoldenBone = Color(0xFFFFD700)           // Gold trophy
val SilverCollar = Color(0xFFC0C0C0)         // Silver trophy
val BronzePaw = Color(0xFFCD7F32)            // Bronze trophy

// Medal Ribbons (for walk medals)
val ChampionRibbon = Color(0xFFFF6B6B)       // 1st place - coral red
val StarRibbon = Color(0xFF4ECDC4)           // 2nd place - teal
val HeartRibbon = Color(0xFFFFE66D)          // 3rd place - sunny yellow

// Achievement Badges
val SuperWalker = Color(0xFF9B59B6)          // Purple achievement
val LoyalFriend = Color(0xFF3498DB)          // Blue achievement
val EarlyBird = Color(0xFFF39C12)            // Orange achievement
val NightOwl = Color(0xFF2C3E50)             // Dark blue achievement

// ════════════════════════════════════════════════════════════════════════════════
// 🎀 SPECIAL ELEMENTS
// ════════════════════════════════════════════════════════════════════════════════
// Adoption Status Colors
val AvailablePet = Color(0xFF81C784)         // Available for adoption
val PendingAdoption = Color(0xFFFFB74D)      // Pending request
val AdoptedLove = Color(0xFFE91E63)          // Adopted (with love!)

// Interactive Elements
val PawPrint = Color(0xFFFFCDD2)             // Soft pink for paw prints
val HeartBeat = Color(0xFFFF7997)            // Favorite/heart color
val Sparkle = Color(0xFFFFE082)              // Highlight/new items

// Outline & Dividers
val SoftOutline = Color(0xFFE8E0DC)          // Light mode outlines
val DarkOutline = Color(0xFF4A4458)          // Dark mode outlines

// ════════════════════════════════════════════════════════════════════════════════
// 🌈 GRADIENTS - For special UI elements
// ════════════════════════════════════════════════════════════════════════════════
val SunsetPaws = Brush.horizontalGradient(
    colors = listOf(PeachyPaw, LavenderWhiskers)
)

val MorningWalk = Brush.horizontalGradient(
    colors = listOf(MintTail, SkyNose)
)

val CelebrationGradient = Brush.horizontalGradient(
    colors = listOf(PeachyPaw, HeartBeat, LavenderWhiskers)
)

val GoldenHour = Brush.verticalGradient(
    colors = listOf(SunnyBelly, PeachyPaw)
)

val CozyEvening = Brush.verticalGradient(
    colors = listOf(LavenderWhiskers, MidnightDen)
)

val RainbowPride = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFF6B6B),  // Red
        Color(0xFFFFE66D),  // Yellow
        Color(0xFF7BD389),  // Green
        Color(0xFF4ECDC4),  // Teal
        Color(0xFF9B59B6)   // Purple
    )
)

val FreshPastelGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFF5F0FA),
        Color(0xFFE8DDD8)
    )
)

// Card Gradient Backgrounds
val WarmCardGradient = Brush.verticalGradient(
    colors = listOf(FluffyWhite, PeachyPawContainer)
)

val CoolCardGradient = Brush.verticalGradient(
    colors = listOf(FluffyWhite, LavenderContainer)
)

val FreshCardGradient = Brush.verticalGradient(
    colors = listOf(FluffyWhite, MintContainer)
)

// Dark Mode Card Gradients
val DarkWarmCardGradient = Brush.verticalGradient(
    colors = listOf(CozyNight, PeachyPawContainerDark)
)

val DarkCoolCardGradient = Brush.verticalGradient(
    colors = listOf(CozyNight, LavenderContainerDark)
)