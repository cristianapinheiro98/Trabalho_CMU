package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import pt.ipp.estg.trabalho_cmu.data.remote.dtos.google.PlaceDetailsResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.google.PlacesNearbyResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for interacting with the Google Places API.
 *
 * Provides endpoints for retrieving:
 * - nearby veterinarians,
 * - detailed information about a particular place.
 */
interface PlacesApiService {

    /**
     * Retrieves nearby veterinary clinics around a given location.
     *
     * @param location Lat,Lon coordinates (e.g. "41.15,-8.61").
     * @param radius Search radius in meters (default 5000).
     * @param type Google Places type filter ("veterinary_care").
     * @param apiKey Google API key.
     *
     * @return PlacesNearbyResponse with list of results.
     */
    @GET("nearbysearch/json")
    suspend fun getNearbyVeterinarians(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,
        @Query("type") type: String = "veterinary_care",
        @Query("key") apiKey: String
    ): PlacesNearbyResponse

    /**
     * Retrieves extra details for a specific place ID.
     *
     * @param placeId Google Places unique ID.
     * @param fields Requested fields (default: phone number + opening hours).
     * @param apiKey Google Places API key.
     *
     * @return PlaceDetailsResponse with detailed information.
     */
    @GET("details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "formatted_phone_number,opening_hours",
        @Query("key") apiKey: String
    ): PlaceDetailsResponse

    companion object {
        /** Base URL used for Google Places API calls. */
        const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    }
}
