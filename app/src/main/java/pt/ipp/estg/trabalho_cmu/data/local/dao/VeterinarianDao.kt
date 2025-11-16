package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian

@Dao
interface VeterinarianDao {
    @Query("SELECT * FROM veterinarians ORDER BY name ASC")
    fun getAllVeterinarians(): LiveData<List<Veterinarian>>

    @Query("SELECT * FROM veterinarians WHERE placeId = :placeId")
    fun getVeterinarianById(placeId: String): LiveData<Veterinarian?>

    @Query("DELETE FROM veterinarians")
    suspend fun deleteAll()

    // Use example: minTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) (24h ago)
    @Query("SELECT * FROM veterinarians WHERE cachedAt > :minTime ORDER BY name ASC")
    fun getValidCachedVeterinarians(minTime: Long): LiveData<List<Veterinarian>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(veterinarian: Veterinarian)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(veterinarians: List<Veterinarian>)

    @Update
    suspend fun update(veterinarian: Veterinarian)

    @Delete
    suspend fun delete(veterinarian: Veterinarian)
}