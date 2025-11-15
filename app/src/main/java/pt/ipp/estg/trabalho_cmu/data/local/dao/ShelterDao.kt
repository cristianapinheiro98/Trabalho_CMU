package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

/**
 * Data Access Object (DAO) for the [Shelter] entity.
 * This interface provides the methods for interacting with the `shelters` table
 * in the application's database.
 */
@Dao
interface ShelterDao {

    /**
     * Retrieves all shelters from the database, ordered alphabetically by name.
     *
     * @return A [LiveData] list of all [Shelter] objects, which automatically
     *         updates observers when the underlying data changes.
     */
    @Query("SELECT * FROM shelters ORDER BY name ASC")
    fun getAllShelters(): LiveData<List<Shelter>>

    /**
     * Retrieves all shelters from the database as a simple list.
     * This is a suspend function and should be called from a coroutine.
     *
     * @return A [List] of all [Shelter] objects.
     */
    @Query("SELECT * FROM shelters ORDER BY name ASC")
    suspend fun getAllSheltersList(): List<Shelter>

    /**
     * Fetches a single shelter by its unique local ID.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param shelterId The local ID of the shelter to retrieve.
     * @return The [Shelter] object if found, otherwise `null`.
     */
    @Query("SELECT * FROM shelters WHERE id = :shelterId LIMIT 1")
    suspend fun getShelterById(shelterId: Int): Shelter?


    /**
     * Fetches a single shelter by its unique Firebase UID.
     * This is useful for linking local data with the remote Firebase user.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param uid The Firebase unique identifier of the shelter's user.
     * @return The [Shelter] object if found, otherwise `null`.
     */
    @Query("SELECT * FROM shelters WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getShelterByFirebaseUid(uid: String): Shelter?

    /**
     * Deletes a shelter from the database using its local ID.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param shelterId The local ID of the shelter to be deleted.
     */
    @Query("DELETE FROM shelters WHERE id = :shelterId")
    suspend fun deleteShelterById(shelterId: Int)

    /**
     * Searches for shelters whose name matches the given query.
     * The search is case-insensitive.
     *
     * @param searchQuery The text to search for within the shelter names.
     * @return A list of [Shelter] objects that match the search query, ordered by name.
     */
    @Query("SELECT * FROM shelters WHERE name LIKE :searchQuery ORDER BY name ASC")
    suspend fun searchSheltersByName(searchQuery: String): List<Shelter>

    /**
     * Counts the total number of shelters in the database.
     * This is a suspend function and must be called from a coroutine.
     *
     * @return The total count of shelters as an [Int].
     */
    @Query("SELECT COUNT(*) FROM shelters")
    suspend fun getShelterCount(): Int

    /**
     * Deletes all records from the `shelters` table.
     * This is a suspend function and must be called from a coroutine.
     */
    @Query("DELETE FROM shelters")
    suspend fun deleteAllShelters()

    /**
     * Inserts a single shelter into the database. If the shelter already exists
     * (based on its primary key), it will be replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param shelter The [Shelter] object to insert.
     * @return The row ID of the newly inserted shelter.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelter(shelter: Shelter) : Long

    /**
     * Inserts a list of shelters into the database. If any shelter already exists,
     * it will be replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param shelters The list of [Shelter] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelters(shelters: List<Shelter>)

    /**
     * Updates an existing shelter in the database.
     * The shelter is identified by its primary key.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param shelter The [Shelter] object with updated information.
     */
    @Update
    suspend fun updateShelter(shelter: Shelter)

    /**
     * Deletes a specific shelter from the database.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param shelter The [Shelter] object to be deleted.
     */
    @Delete
    suspend fun deleteShelter(shelter: Shelter)

    /**
     * Synchronously inserts a list of shelters.
     * This method is not a suspend function and is intended for use in contexts
     * where coroutines are not available, such as database seeding callbacks.
     *
     * @param shelters The list of [Shelter] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSync(shelters: List<Shelter>)

}
