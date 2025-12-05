package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

@Dao
interface ShelterDao {
    @Query("SELECT * FROM shelters ORDER BY name ASC")
    fun getAllShelters(): LiveData<List<Shelter>>

    @Query("SELECT * FROM shelters ORDER BY name ASC")
    suspend fun getAllSheltersList(): List<Shelter>

    @Query("SELECT * FROM shelters WHERE id = :shelterId LIMIT 1")
    suspend fun getShelterById(shelterId: String): Shelter?

    @Query("SELECT * FROM shelters WHERE name LIKE :searchQuery ORDER BY name ASC")
    suspend fun searchSheltersByName(searchQuery: String): List<Shelter>

    @Query("SELECT COUNT(*) FROM shelters")
    suspend fun getShelterCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shelter: Shelter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shelters: List<Shelter>)

    @Update
    suspend fun update(shelter: Shelter)

    @Delete
    suspend fun delete(shelter: Shelter)

    @Query("DELETE FROM shelters")
    suspend fun deleteAll()

    // --- MÉTODOS EM FALTA ADICIONADOS ---
    @Transaction
    suspend fun refreshCache(shelters: List<Shelter>) {
        deleteAll()
        insertAll(shelters)
    }
}