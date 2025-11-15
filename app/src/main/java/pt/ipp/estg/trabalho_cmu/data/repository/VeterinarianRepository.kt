package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.VeterinarianDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.google.PlaceDetails
import pt.ipp.estg.trabalho_cmu.di.RetrofitInstance
import java.util.Calendar

class VeterinarianRepository(
    private val veterinarianDao: VeterinarianDao
) {
    private val placesApi = RetrofitInstance.placesApi
    private val apiKey = RetrofitInstance.placesApiKey
    private val CACHE_DURATION = 24 * 60 * 60 * 1000L // 24 horas

    fun getAllVeterinarians(): LiveData<List<Veterinarian>> {
        return veterinarianDao.getAllVeterinarians()
    }

    fun getVeterinarianById(placeId: String): LiveData<Veterinarian?> {
        return veterinarianDao.getVeterinarianById(placeId)
    }

    suspend fun refreshVeterinariansFromAPI(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            val location = "$latitude,$longitude"

            // Try bigger radius if not enough veterinarian centers near user
            val radii = listOf(5000, 10000, 20000) // 5km, 10km, 20km
            var veterinarians: List<Veterinarian> = emptyList()

            for (radius in radii) {
                veterinarians = fetchVeterinarians(location, radius)

                if (veterinarians.size >= 3) {
                    break
                }
            }

            // Cleans old cache and inserts new data
            if (veterinarians.isNotEmpty()) {
                veterinarianDao.deleteAll()
                veterinarianDao.insertAll(veterinarians)
            }
        }
    }

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
                            phone = details?.formattedPhoneNumber ?: "Sem telefone",
                            isOpenNow = place.openingHours?.openNow ?: false,
                            todaySchedule = getTodaySchedule(details?.openingHours?.weekdayText),
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

    // Extrai o horário de hoje da lista completa
    private fun getTodaySchedule(weekdayText: List<String>?): String {
        if (weekdayText.isNullOrEmpty()) return "Horário não disponível"

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        // Google Places API returns: ["Monday: 9:00 AM – 6:00 PM", ...]
        // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
        // weekdayText array: 0=Monday, 1=Tuesday, ..., 6=Sunday

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

        return weekdayText.getOrNull(index) ?: "Horário não disponível"
    }

    // Verifica se cache é válido (< 24h)
    fun isCacheValid(veterinarians: List<Veterinarian>): Boolean {
        if (veterinarians.isEmpty()) return false

        val now = System.currentTimeMillis()
        val oldestCache = veterinarians.minOfOrNull { it.cachedAt } ?: 0

        return (now - oldestCache) < CACHE_DURATION
    }
}