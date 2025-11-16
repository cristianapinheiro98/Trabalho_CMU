package pt.ipp.estg.trabalho_cmu.data.remote.dtos.google

import com.google.gson.annotations.SerializedName

// Resposta da API Nearby Search
data class PlacesNearbyResponse(
    val results: List<PlaceResult>,
    val status: String
)

data class PlaceResult(
    @SerializedName("place_id")
    val placeId: String,
    val name: String,
    val vicinity: String,  // Simplified address
    val geometry: Geometry,
    val rating: Double? = null,
    @SerializedName("opening_hours")
    val openingHours: OpeningHours? = null
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean? = null
)

// Resposta da API Place Details (para telefone e horários completos)
data class PlaceDetailsResponse(
    val result: PlaceDetails,
    val status: String
)

data class PlaceDetails(
    @SerializedName("formatted_phone_number")
    val formattedPhoneNumber: String? = null,
    @SerializedName("opening_hours")
    val openingHours: DetailedOpeningHours? = null
)

data class DetailedOpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean? = null,
    @SerializedName("weekday_text")
    val weekdayText: List<String>? = null
)