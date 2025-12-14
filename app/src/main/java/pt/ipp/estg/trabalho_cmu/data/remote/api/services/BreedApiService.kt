package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.CatBreedsResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.DogBreedsResponse
import retrofit2.Call
import retrofit2.http.GET

/**
 * Retrofit service for fetching dog breeds from the remote API.
 *
 * The endpoint returns a list of dog breed objects.
 */
interface DogApiService {
    /**
     * Retrieves all dog breeds available in the API.
     *
     * @return Call wrapping a list of DogBreedsResponse models.
     */
    @GET("breeds")
    fun getAllBreeds(): Call<List<DogBreedsResponse>>
}

/**
 * Retrofit service for fetching cat breeds from the remote API.
 */
interface CatApiService {
    /**
     * Retrieves all cat breeds available in the API.
     *
     * @return Call wrapping a list of CatBreedsResponse models.
     */
    @GET("breeds")
    fun getAllBreeds(): Call<List<CatBreedsResponse>>
}
