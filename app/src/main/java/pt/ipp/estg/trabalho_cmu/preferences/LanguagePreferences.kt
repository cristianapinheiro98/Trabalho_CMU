package pt.ipp.estg.trabalho_cmu.preferences

import android.content.Context
import android.content.SharedPreferences

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
