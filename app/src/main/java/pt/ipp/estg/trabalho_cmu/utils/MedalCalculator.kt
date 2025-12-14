package pt.ipp.estg.trabalho_cmu.utils

/**
 * Medal calculation utility for walk achievements
 * Provides configurable medal criteria based on walk metrics
 */
object MedalCalculator {

    /**
     * Available medal types
     */
    enum class MedalType {
        BRONZE,
        SILVER,
        GOLD
    }

    /**
     * Configurable medal criteria
     * @property bronzeThreshold Minimum value for bronze medal
     * @property silverThreshold Minimum value for silver medal
     * @property goldThreshold Minimum value for gold medal
     */
    private data class MedalCriteria(
        val bronzeThreshold: Long,   // seconds or meters depending on strategy
        val silverThreshold: Long,
        val goldThreshold: Long
    )

    /**
     * Current medal criteria (duration-based for testing)
     * TESTING CONFIGURATION:
     * - Bronze: 15 seconds
     * - Silver: 30 seconds
     * - Gold: 45+ seconds
     *
     * PRODUCTION CONFIGURATION (uncomment when ready):
     * Replace with distance-based criteria (see calculateMedalByDistance)
     */
    private val currentCriteria = MedalCriteria(
        bronzeThreshold = 15,      // 15 seconds
        silverThreshold = 30,      // 30 seconds
        goldThreshold = 45         // 45 seconds
    )

    /**
     * Calculate medal based on walk duration (current implementation)
     * @param durationSeconds Walk duration in seconds
     * @return MedalType or null if no medal earned
     */
    fun calculateMedal(durationSeconds: Long): MedalType? {
        return when {
            durationSeconds < currentCriteria.bronzeThreshold -> null
            durationSeconds < currentCriteria.silverThreshold -> MedalType.BRONZE
            durationSeconds < currentCriteria.goldThreshold -> MedalType.SILVER
            else -> MedalType.GOLD
        }
    }

    /**
     * Calculate medal based on distance
     *
     * To switch to distance-based medals:
     * 1. Uncomment this method
     * 2. Update currentCriteria with distance thresholds (in meters)
     * 3. Update calculateMedal() to call this method instead
     *
     * Example distance criteria:
     * - Bronze: 1000m (1km)
     * - Silver: 3000m (3km)
     * - Gold: 5000m+ (5km+)
     *
     * @param distanceMeters Walk distance in meters
     * @return MedalType or null if no medal earned
     */
    /*
    fun calculateMedalByDistance(distanceMeters: Double): MedalType? {
        val distanceKm = distanceMeters / 1000.0
        return when {
            distanceKm < 1.0 -> null
            distanceKm < 3.0 -> MedalType.BRONZE
            distanceKm < 5.0 -> MedalType.SILVER
            else -> MedalType.GOLD
        }
    }
    */

    /**
     * Get medal emoji representation
     * @param medalType Medal type to get emoji for
     * @return Emoji string
     */
    fun getMedalEmoji(medalType: MedalType): String {
        return when (medalType) {
            MedalType.BRONZE -> "🥉"
            MedalType.SILVER -> "🥈"
            MedalType.GOLD -> "🥇"
        }
    }
}
