package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimalApiService {

    // Endpoint GET /animais?sortBy=...&order=...
    @GET("animais")
    suspend fun getAnimais(
        @Query("species") species: String? = null,          // tipo de animal
        @Query("age") age: Int? = null,                     // idade
        @Query("size") size: String? = null,                // porte
        @Query("color") color: String? = null,              // cor
        @Query("gender") gender: String? = null,            // sexo
        @Query("shelterName") shelterName: String? = null,  // nome do abrigo
        @Query("shelterAddress") shelterAddress: String? = null, // morada do abrigo
        @Query("breed") breed: String? = null,              // raça
        @Query("sortBy") sortBy: String? = null,            // campo para ordenar
        @Query("order") order: String? = null               // asc / desc
    ): List<Animal>
}