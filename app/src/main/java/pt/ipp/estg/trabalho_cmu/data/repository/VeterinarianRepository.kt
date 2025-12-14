package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.VeterinarianDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.google.PlaceDetails
import pt.ipp.estg.trabalho_cmu.providers.RetrofitInstance
import java.util.Calendar

/**
 * Repository responsible for managing veterinarian data.
 *
 * This repository fetches nearby veterinary centers from Google Places API
 * and caches them locally in Room for offline access.
 *
 * Features:
 * - Fetches nearby veterinarians using Google Places API
 * - Caches results in Room database for 24 hours
 * - Automatically expands search radius if not enough results
 * - Provides both LiveData observation and direct cache access
 *
 * @property veterinarianDao DAO for local veterinarian storage.
 */
class VeterinarianRepository(
    private val veterinarianDao: VeterinarianDao
) {
    private val placesApi = RetrofitInstance.placesApi
    private val apiKey = RetrofitInstance.placesApiKey

    companion object {
        /** Cache validity duration: 24 hours in milliseconds. */
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L

        /** Minimum number of results before trying a larger radius. */
        private const val MIN_RESULTS_THRESHOLD = 3

        /** Search radii to try in order (meters). */
        private val SEARCH_RADII = listOf(5000, 10000, 20000) // 5km, 10km, 20km
    }

    /**
     * Returns a LiveData list of all cached veterinarians.
     * Useful for reactive UI updates.
     *
     * @return LiveData containing the list of veterinarians.
     */
    fun getAllVeterinarians(): LiveData<List<Veterinarian>> {
        return veterinarianDao.getAllVeterinarians()
    }

    /**
     * Returns a LiveData of a single veterinarian by its Place ID.
     *
     * @param placeId Google Places API place ID.
     * @return LiveData containing the veterinarian or null if not found.
     */
    fun getVeterinarianById(placeId: String): LiveData<Veterinarian?> {
        return veterinarianDao.getVeterinarianById(placeId)
    }

    /**
     * Retrieves all cached veterinarians from the local database.
     * This is a suspend function for use in coroutines without LiveData.
     *
     * @return List of cached veterinarians sorted by rating.
     */
    suspend fun getCachedVeterinarians(): List<Veterinarian> = withContext(Dispatchers.IO) {
        veterinarianDao.getAllVeterinariansList()
    }

    /**
     * Fetches nearby veterinarians from Google Places API and caches them locally.
     *
     * The method progressively increases the search radius (5km, 10km, 20km)
     * until at least 3 results are found or all radii are exhausted.
     *
     * @param latitude User's current latitude.
     * @param longitude User's current longitude.
     */
    suspend fun refreshVeterinariansFromAPI(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            val location = "$latitude,$longitude"
            var veterinarians: List<Veterinarian> = emptyList()

            for (radius in SEARCH_RADII) {
                veterinarians = fetchVeterinarians(location, radius)

                if (veterinarians.size >= MIN_RESULTS_THRESHOLD) {
                    break
                }
            }

            if (veterinarians.isNotEmpty()) {
                veterinarianDao.deleteAll()
                veterinarianDao.insertAll(veterinarians)
            }
        }
    }

    /**
     * Fetches veterinarians from Google Places Nearby Search API.
     *
     * @param location Location string in "latitude,longitude" format.
     * @param radius Search radius in meters.
     * @return List of veterinarians found within the radius.
     */
    private suspend fun fetchVeterinarians(location: String, radius: Int): List<Veterinarian> {
        return try {
            val response = placesApi.getNearbyVeterinarians(
                location = location,
                radius = radius,
                apiKey = apiKey
            )

            if (response.status == "OK") {
                response.results.mapNotNull { place ->
                    try {
                        val details = fetchPlaceDetails(place.placeId)

                        Veterinarian(
                            placeId = place.placeId,
                            name = place.name,
                            address = place.vicinity,
                            phone = details?.formattedPhoneNumber ?: "No phone available",
                            isOpenNow = place.openingHours?.openNow ?: false,
                            todaySchedule = extractTodaySchedule(details?.openingHours?.weekdayText),
                            weekdaySchedules = details?.openingHours?.weekdayText?.joinToString("|") ?: "",
                            latitude = place.geometry.location.lat,
                            longitude = place.geometry.location.lng,
                            rating = place.rating
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches detailed information for a specific place from Google Places Details API.
     *
     * @param placeId Google Places API place ID.
     * @return Place details or null if the request fails.
     */
    private suspend fun fetchPlaceDetails(placeId: String): PlaceDetails? {
        return try {
            val response = placesApi.getPlaceDetails(
                placeId = placeId,
                apiKey = apiKey
            )

            if (response.status == "OK") {
                response.result
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts today's schedule from the weekday text list.
     *
     * Google Places API returns weekday text as:
     * ["Monday: 9:00 AM – 6:00 PM", "Tuesday: ...", ...]
     *
     * Calendar.DAY_OF_WEEK mapping: 1=Sunday, 2=Monday, ..., 7=Saturday
     * weekdayText array mapping: 0=Monday, 1=Tuesday, ..., 6=Sunday
     *
     * @param weekdayText List of weekday schedules from Google Places API.
     * @return Today's schedule string or a default message if unavailable.
     */
    private fun extractTodaySchedule(weekdayText: List<String>?): String {
        if (weekdayText.isNullOrEmpty()) return "Schedule not available"

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        val index = when (today) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        return weekdayText.getOrNull(index) ?: "Schedule not available"
    }

    /**
     * Checks if the cached veterinarians are still valid.
     *
     * Cache is considered valid if it's less than 24 hours old.
     *
     * @param veterinarians List of cached veterinarians.
     * @return True if cache is valid, false if expired or empty.
     */
    fun isCacheValid(veterinarians: List<Veterinarian>): Boolean {
        if (veterinarians.isEmpty()) return false

        val now = System.currentTimeMillis()
        val oldestCache = veterinarians.minOfOrNull { it.cachedAt } ?: 0

        return (now - oldestCache) < CACHE_DURATION_MS
    }
}