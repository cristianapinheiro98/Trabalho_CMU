package pt.ipp.estg.trabalho_cmu.data.remote.api.services


import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.CatBreedsResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.DogBreedsResponse
import retrofit2.Call
import retrofit2.http.GET


interface DogApiService {
    @GET("pt/ipp/estg/trabalho_cmu/data/remote/api/breeds/list/all")
    fun getAllBreeds(): Call<DogBreedsResponse>
}

interface CatApiService {
    @GET("breeds")
    fun getAllBreeds(): Call<List<CatBreedsResponse>>
}