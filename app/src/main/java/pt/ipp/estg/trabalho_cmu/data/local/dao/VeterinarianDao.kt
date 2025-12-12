package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian

/**
 * DAO for managing veterinarian data in the local Room database.
 *
 * Provides methods for:
 * - Querying cached veterinarians (LiveData and suspend variants)
 * - Inserting/updating veterinarian records
 * - Cache management with TTL support
 */
@Dao
interface VeterinarianDao {

    /**
     * Retrieves all veterinarians as LiveData, sorted alphabetically by name.
     * Useful for reactive UI updates.
     *
     * @return LiveData containing the list of all cached veterinarians.
     */
    @Query("SELECT * FROM veterinarians ORDER BY name ASC")
    fun getAllVeterinarians(): LiveData<List<Veterinarian>>

    /**
     * Retrieves all veterinarians as a suspend List, sorted by rating (highest first).
     * Useful for one-time data fetching in coroutines.
     *
     * @return List of all cached veterinarians.
     */
    @Query("SELECT * FROM veterinarians ORDER BY rating DESC")
    suspend fun getAllVeterinariansList(): List<Veterinarian>

    /**
     * Retrieves a single veterinarian by its Google Place ID.
     *
     * @param placeId The Google Places API place ID.
     * @return LiveData containing the veterinarian or null if not found.
     */
    @Query("SELECT * FROM veterinarians WHERE placeId = :placeId")
    fun getVeterinarianById(placeId: String): LiveData<Veterinarian?>

    /**
     * Retrieves veterinarians cached after a specific timestamp.
     * Useful for filtering out expired cache entries.
     *
     * Example usage for 24h TTL:
     * ```
     * val minTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
     * dao.getValidCachedVeterinarians(minTime)
     * ```
     *
     * @param minTime Minimum cachedAt timestamp to include.
     * @return LiveData containing veterinarians cached after minTime.
     */
    @Query("SELECT * FROM veterinarians WHERE cachedAt > :minTime ORDER BY name ASC")
    fun getValidCachedVeterinarians(minTime: Long): LiveData<List<Veterinarian>>

    /**
     * Deletes all veterinarians from the cache.
     * Called before refreshing data from the API.
     */
    @Query("DELETE FROM veterinarians")
    suspend fun deleteAll()

    /**
     * Inserts a single veterinarian into the database.
     * If a record with the same placeId exists, it will be replaced.
     *
     * @param veterinarian The veterinarian to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(veterinarian: Veterinarian)

    /**
     * Inserts multiple veterinarians into the database.
     * Existing records with the same placeId will be replaced.
     *
     * @param veterinarians List of veterinarians to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(veterinarians: List<Veterinarian>)

    /**
     * Updates an existing veterinarian record.
     *
     * @param veterinarian The veterinarian with updated data.
     */
    @Update
    suspend fun update(veterinarian: Veterinarian)

    /**
     * Deletes a specific veterinarian from the database.
     *
     * @param veterinarian The veterinarian to delete.
     */
    @Delete
    suspend fun delete(veterinarian: Veterinarian)
}