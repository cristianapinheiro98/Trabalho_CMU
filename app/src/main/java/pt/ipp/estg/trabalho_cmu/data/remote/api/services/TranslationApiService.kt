package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import pt.ipp.estg.trabalho_cmu.data.remote.dtos.TranslationRequest
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.TranslationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit service for requesting text translations.
 *
 * This endpoint accepts a JSON body containing the text to translate.
 */
interface TranslationApiService {

    /**
     * Sends a translation request to the API.
     *
     * @param request Object containing the text and languages.
     * @return Call wrapping a TranslationResponse.
     */
    @POST("translate")
    @Headers("Content-Type: application/json")
    fun translate(@Body request: TranslationRequest): Call<TranslationResponse>
}
