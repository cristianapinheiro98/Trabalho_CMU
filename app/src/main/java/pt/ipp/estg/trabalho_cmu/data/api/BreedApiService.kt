package pt.ipp.estg.trabalho_cmu.data.api


import pt.ipp.estg.trabalho_cmu.data.models.CatBreedResponse
import pt.ipp.estg.trabalho_cmu.data.models.DogBreedsResponse
import retrofit2.Call
import retrofit2.http.GET


interface DogApiService {
    @GET("api/breeds/list/all")
    fun getAllBreeds(): Call<DogBreedsResponse>
}



interface CatApiService {
    @GET("breeds")
    fun getAllBreeds(): Call<List<CatBreedResponse>>
}