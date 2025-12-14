package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.remote.dtos.translation.TranslationRequest
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.translation.TranslationResponse
import pt.ipp.estg.trabalho_cmu.providers.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository responsible for translating text using a remote Translation API.
 *
 * This class sends translation requests through Retrofit and exposes results
 * through two callback functions:
 *  - onSuccess: returns the translated text (or the original text if unavailable)
 *  - onError: returns a UI-facing string (R.string.*)
 *
 * All strings used for UI messages have been moved to localized resources.
 */
class TranslationRepository {

    private val api = RetrofitInstance.translateApi

    /**
     * Translates text from English to Portuguese.
     *
     * @param text The text to translate.
     * @param onSuccess Callback invoked with the translated string.
     * @param onError Callback invoked with a string resource key (R.string.*) on failure.
     */
    fun translateToPortuguese(
        text: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = TranslationRequest(
            q = text,
            source = "en",
            target = "pt"
        )

        api.translate(request).enqueue(object : Callback<TranslationResponse> {

            override fun onResponse(
                call: Call<TranslationResponse>,
                response: Response<TranslationResponse>
            ) {
                if (response.isSuccessful) {
                    val translated = response.body()?.translatedText ?: text
                    onSuccess(translated)
                } else {
                    onError("Translation failed")
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                onError("Error network")
            }
        })
    }
}
