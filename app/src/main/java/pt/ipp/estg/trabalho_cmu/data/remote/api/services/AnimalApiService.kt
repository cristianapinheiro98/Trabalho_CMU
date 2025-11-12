package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimalApiService {

    // Endpoint GET /animais?sortBy=...&order=...
    @GET("animais")
    suspend fun getAnimais(
        @Query("species") species: String? = null,
        @Query("size") size: String? = null,
        @Query("gender") gender: String? = null,
        @Query("shelterName") shelterName: String? = null,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("order") order: String? = "desc"
    ): List<Animal>
}