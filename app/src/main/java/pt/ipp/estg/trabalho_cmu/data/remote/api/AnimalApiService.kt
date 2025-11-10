package pt.ipp.estg.trabalho_cmu.data.remote.api

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimalApiService {

    // Endpoint GET /animais?sortBy=...&order=...
    @GET("animais")
    suspend fun getAnimais(
        @Query("sortBy") sortBy: String? = null,
        @Query("order") order: String? = null
    ): List<Animal>
}