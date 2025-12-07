package pt.ipp.estg.trabalho_cmu.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility object that manages storing and retrieving the selected app language
 * using SharedPreferences.
 *
 * - saveLanguage(): persists a language code such as "en", "pt", etc.
 * - getLanguage(): retrieves the saved language, defaulting to English ("en")
 *
 * This class does not display UI messages nor throw user-facing exceptions.
 */

object LanguagePreferences {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "language"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_LANGUAGE, languageCode)
        editor.commit()
    }

    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }
}
