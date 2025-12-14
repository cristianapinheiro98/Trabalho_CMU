package pt.ipp.estg.trabalho_cmu.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

/**
 * Helper object responsible for updating the application's runtime locale.
 *
 * This utility allows switching the app language dynamically while the app
 * is running. It creates a new configuration context with the selected locale,
 * which must then be applied in `attachBaseContext()` in the Activity.
 *
 * @param context The base context used by the application/activity.
 * @param language A language code (e.g., "pt", "en").
 * @return A new context configured with the requested locale.
 */
object LocaleHelper {
    fun setLocale(base: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = base.resources.configuration
        config.setLocale(locale)

        return base.createConfigurationContext(config)
    }
}

