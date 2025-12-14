package pt.ipp.estg.trabalho_cmu.data.remote.dtos.google

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object representing the response from the Google Places "Nearby Search" API.
 *
 * This class wraps the list of places found near a specific location.
 *
 * @property results List of [PlaceResult] objects containing summary information about each place.
 * @property status The status code returned by the API (e.g., "OK", "ZERO_RESULTS").
 */
data class PlacesNearbyResponse(
    val results: List<PlaceResult>,
    val status: String
)

/**
 * Represents a single place returned within the nearby search results.
 *
 * Contains summary information required to display markers on a map or a list item.
 *
 * @property placeId Unique identifier for the place (used to fetch details later).
 * @property name The name of the place.
 * @property vicinity A simplified address or neighborhood description (e.g., "Main Street, New York").
 * @property geometry Contains the geographical coordinates (latitude/longitude) for map placement.
 * @property rating The aggregate user rating (from 1.0 to 5.0), if available.
 * @property openingHours Basic opening hours information (specifically if it is open now).
 */
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

/**
 * Container for the location data within the API response.
 *
 * @property location The specific latitude and longitude object.
 */
data class Geometry(
    val location: Location
)

/**
 * Represents geographical coordinates.
 *
 * @property lat Latitude value.
 * @property lng Longitude value.
 */
data class Location(
    val lat: Double,
    val lng: Double
)

/**
 * Simplified opening hours data used in search results.
 *
 * @property openNow Boolean indicating if the place is currently open at the time of the request.
 */
data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean? = null
)

/**
* Data Transfer Object representing the response from the Google Places "Place Details" API.
*
* This is used when requesting specific information for a single place using its [PlaceResult.placeId].
*
* @property result The detailed information object for the requested place.
* @property status The status code returned by the API.
*/
data class PlaceDetailsResponse(
    val result: PlaceDetails,
    val status: String
)

/**
 * Detailed information about a specific place.
 *
 * Unlike [PlaceResult], this object may contain richer data such as phone numbers and full schedules.
 *
 * @property formattedPhoneNumber The place's phone number formatted for local display.
 * @property openingHours Detailed opening hours including weekday text descriptions.
 */
data class PlaceDetails(
    @SerializedName("formatted_phone_number")
    val formattedPhoneNumber: String? = null,
    @SerializedName("opening_hours")
    val openingHours: DetailedOpeningHours? = null
)

/**
 * Comprehensive opening hours information.
 *
 * @property openNow Boolean indicating if the place is currently open.
 * @property weekdayText List of strings describing the opening hours for each day of the week.
 */
data class DetailedOpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean? = null,
    @SerializedName("weekday_text")
    val weekdayText: List<String>? = null
)