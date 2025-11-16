package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import pt.ipp.estg.trabalho_cmu.data.remote.dtos.google.PlaceDetailsResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.google.PlacesNearbyResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

    @GET("nearbysearch/json")
    suspend fun getNearbyVeterinarians(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,  // meters
        @Query("type") type: String = "veterinary_care",
        @Query("key") apiKey: String
    ): PlacesNearbyResponse

    @GET("details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "formatted_phone_number,opening_hours",
        @Query("key") apiKey: String
    ): PlaceDetailsResponse

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    }
}
