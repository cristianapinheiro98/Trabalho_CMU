package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

/**
 * DAO handling all operations related to local Shelter storage.
 * Supports listing, searching, inserting, updating, deleting, and cache refresh.
 */
@Dao
interface ShelterDao {

    /**
     * Returns all shelters sorted alphabetically.
     * LiveData version for reactive updates.
     */
    @Query("SELECT * FROM shelters ORDER BY name ASC")
    fun getAllShelters(): LiveData<List<Shelter>>

    /**
     * Same as getAllShelters(), but returns a List.
     */
    @Query("SELECT * FROM shelters ORDER BY name ASC")
    suspend fun getAllSheltersList(): List<Shelter>

    /**
     * Retrieves a shelter by ID.
     */
    @Query("SELECT * FROM shelters WHERE id = :shelterId LIMIT 1")
    suspend fun getShelterById(shelterId: String): Shelter?

    /**
     * Searches shelters by name using a LIKE query.
     */
    @Query("SELECT * FROM shelters WHERE name LIKE :searchQuery ORDER BY name ASC")
    suspend fun searchSheltersByName(searchQuery: String): List<Shelter>

    /**
     * Counts the total number of shelters in storage.
     */
    @Query("SELECT COUNT(*) FROM shelters")
    suspend fun getShelterCount(): Int

    /**
     * Inserts or replaces a shelter.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shelter: Shelter)

    /**
     * Inserts or replaces a list of shelters.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shelters: List<Shelter>)

    /**
     * Updates an existing shelter.
     */
    @Update
    suspend fun update(shelter: Shelter)

    /**
     * Deletes a single shelter.
     */
    @Delete
    suspend fun delete(shelter: Shelter)

    /**
     * Deletes all shelters.
     */
    @Query("DELETE FROM shelters")
    suspend fun deleteAll()

    /**
     * Clears and repopulates the shelter table. Used during sync.
     */
    @Transaction
    suspend fun refreshCache(shelters: List<Shelter>) {
        deleteAll()
        insertAll(shelters)
    }
}
