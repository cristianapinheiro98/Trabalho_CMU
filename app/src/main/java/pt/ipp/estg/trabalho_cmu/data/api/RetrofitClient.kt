package pt.ipp.estg.trabalho_cmu.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val DOG_API_BASE_URL = "https://dog.ceo/"
    private const val CAT_API_BASE_URL = "https://api.thecatapi.com/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Dog API
    private val dogRetrofit = Retrofit.Builder()
        .baseUrl(DOG_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val dogApiService: DogApiService = dogRetrofit.create(DogApiService::class.java)

    // Cat API
    private val catRetrofit = Retrofit.Builder()
        .baseUrl(CAT_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val catApiService: CatApiService = catRetrofit.create(CatApiService::class.java)
}