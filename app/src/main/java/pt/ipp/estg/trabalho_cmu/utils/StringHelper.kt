package pt.ipp.estg.trabalho_cmu.utils

import android.content.Context
import androidx.annotation.StringRes
import pt.ipp.estg.trabalho_cmu.preferences.LanguagePreferences

/**
 * Helper object to retrieve localized strings based on user's language preferences.
 *
 * This utility ensures that repositories, ViewModels, and other components that use
 * Application Context can still get properly localized strings according to the
 * user's selected language (stored in SharedPreferences), rather than the system locale.
 */
object StringHelper {

    /**
     * Retrieves a localized string resource.
     *
     * @param context Any context (preferably Application context for repositories/ViewModels)
     * @param resId The string resource ID
     * @param formatArgs Optional format arguments if the string contains placeholders
     * @return The localized string according to user preferences
     */
    fun getString(context: Context, @StringRes resId: Int, vararg formatArgs: Any): String {
        val language = LanguagePreferences.getLanguage(context)
        val localizedContext = LocaleHelper.setLocale(context, language)

        return if (formatArgs.isEmpty()) {
            localizedContext.getString(resId)
        } else {
            localizedContext.getString(resId, *formatArgs)
        }
    }

    /**
     * Retrieves a localized plural string resource.
     *
     * @param context Any context
     * @param resId The plural string resource ID
     * @param quantity The quantity to determine which plural form to use
     * @param formatArgs Optional format arguments
     * @return The localized plural string
     */
    fun getQuantityString(
        context: Context,
        @StringRes resId: Int,
        quantity: Int,
        vararg formatArgs: Any
    ): String {
        val language = LanguagePreferences.getLanguage(context)
        val localizedContext = LocaleHelper.setLocale(context, language)

        return if (formatArgs.isEmpty()) {
            localizedContext.resources.getQuantityString(resId, quantity)
        } else {
            localizedContext.resources.getQuantityString(resId, quantity, *formatArgs)
        }
    }
}