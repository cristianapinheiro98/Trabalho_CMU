package pt.ipp.estg.trabalho_cmu.providers

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pt.ipp.estg.trabalho_cmu.BuildConfig
import pt.ipp.estg.trabalho_cmu.data.remote.api.services.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val DOG_API_BASE_URL =  "https://api.thedogapi.com/v1/"
    private const val CAT_API_BASE_URL = "https://api.thecatapi.com/v1/"
    private const val LOCAL_API_BASE_URL = "http://10.0.2.2:3000/api/"
    private const val TRANSLATE_API_BASE_URL = "https://libretranslate.de/"
    private const val PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/"

    // Logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Shared OKHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder().setLenient().create()
    val placesApiKey: String = BuildConfig.GOOGLE_PLACES_API_KEY

    // ---------------- DOG API ----------------
    private val dogRetrofit = Retrofit.Builder()
        .baseUrl(DOG_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val dogApi: DogApiService by lazy {
        dogRetrofit.create(DogApiService::class.java)
    }

    // ---------------- CAT API ----------------
    private val catRetrofit = Retrofit.Builder()
        .baseUrl(CAT_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val catApi: CatApiService by lazy {
        catRetrofit.create(CatApiService::class.java)
    }

    // ---------------- TRANSLATE API ----------------
    private val translateRetrofit = Retrofit.Builder()
        .baseUrl(TRANSLATE_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val translateApi: TranslationApiService by lazy {
        translateRetrofit.create(TranslationApiService::class.java)
    }

    // ---------------- LOCAL BACKEND ----------------
    private val localRetrofit = Retrofit.Builder()
        .baseUrl(LOCAL_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // ---------------- GOOGLE PLACES API ----------------
    private val placesRetrofit = Retrofit.Builder()
        .baseUrl(PLACES_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val placesApi: PlacesApiService by lazy {
        placesRetrofit.create(PlacesApiService::class.java)
    }
}
