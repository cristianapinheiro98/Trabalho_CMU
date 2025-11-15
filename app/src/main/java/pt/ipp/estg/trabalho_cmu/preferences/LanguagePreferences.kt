package pt.ipp.estg.trabalho_cmu.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * A singleton object to manage the application's language preferences.
 *
 * This object provides a simple interface to save and retrieve the user's selected
 * language code using [SharedPreferences]. It ensures that the language setting
 * persists across application sessions.
 */
object LanguagePreferences {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "language"

    /**
     * Retrieves the private [SharedPreferences] instance for the application.
     *
     * @param context The application context used to access shared preferences.
     * @return The [SharedPreferences] instance named [PREFS_NAME].
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves the selected language code to SharedPreferences.
     *
     * This function synchronously commits the language code to persistent storage.
     *
     * @param context The application context.
     * @param languageCode The ISO 639-1 language code to save (e.g., "en", "pt").
     */
    fun saveLanguage(context: Context, languageCode: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_LANGUAGE, languageCode)
        editor.commit()
    }


    /**
     * Retrieves the saved language code from SharedPreferences.
     *
     * If no language code is found, it defaults to "en" (English).
     *
     * @param context The application context.
     * @return The saved language code as a [String]. Defaults to "en".
     */
    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }
}
