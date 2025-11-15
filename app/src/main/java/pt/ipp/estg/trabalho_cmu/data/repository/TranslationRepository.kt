package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.remote.dtos.TranslationRequest
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.TranslationResponse
import pt.ipp.estg.trabalho_cmu.di.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslationRepository {
    private val api = RetrofitInstance.translateApi

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
                    onError("Erro ao traduzir: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                onError("Erro de rede: ${t.message}")
            }
        })
    }
}
