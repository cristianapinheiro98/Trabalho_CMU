package pt.ipp.estg.trabalho_cmu.di

import pt.ipp.estg.trabalho_cmu.data.remote.api.AnimalApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    val api: AnimalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnimalApiService::class.java)
    }
}