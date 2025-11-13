package pt.ipp.estg.trabalho_cmu.data.remote.api.services


import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.CatBreedResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.DogBreedResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.CatBreedsResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.DogBreedsResponse
import retrofit2.Call
import retrofit2.http.GET


interface DogApiService {
    @GET("breeds")
    fun getAllBreeds(): Call<List<DogBreedResponse>>
}



interface CatApiService {
    @GET("breeds")
    fun getAllBreeds(): Call<List<CatBreedsResponse>>
}