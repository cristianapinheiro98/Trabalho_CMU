package pt.ipp.estg.trabalho_cmu.data.remote.dtos

import com.google.gson.annotations.SerializedName

/**
 * Request body used for translation API calls.
 *
 * @property q Text to translate.
 * @property source Source language code (default "en").
 * @property target Target language code (default "pt").
 * @property format Input format (text/plain).
 */
data class TranslationRequest(
    val q: String,
    val source: String = "en",
    val target: String = "pt",
    val format: String = "text"
)

/**
 * Response model returned after a translation request.
 *
 * @property translatedText The resulting translated text.
 */
data class TranslationResponse(
    @SerializedName("translatedText")
    val translatedText: String
)
