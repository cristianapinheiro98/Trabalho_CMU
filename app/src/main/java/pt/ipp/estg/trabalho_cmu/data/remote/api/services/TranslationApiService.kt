package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import pt.ipp.estg.trabalho_cmu.data.remote.dtos.TranslationRequest
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.TranslationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TranslationApiService {
    @POST("translate")
    @Headers("Content-Type: application/json")
    fun translate(@Body request: TranslationRequest): Call<TranslationResponse>
}

