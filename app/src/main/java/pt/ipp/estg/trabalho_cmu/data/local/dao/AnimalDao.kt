package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

/**
 * DAO responsible for managing local storage of Animal entities.
 * Provides queries for listing, filtering, sorting, searching,
 * inserting, updating, and refreshing cached data.
 */
@Dao
interface AnimalDao {

    /**
     * Returns all animals with AVAILABLE status, ordered by creation date (newest first).
     * LiveData version for reactive UI updates.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun getAllAnimals(): LiveData<List<Animal>>

    /**
     * Same as getAllAnimals(), but returns a normal List for coroutine use.
     */
    @Query("SELECT * FROM animals  WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun getAllAnimalsList(): List<Animal>

    /**
     * Retrieves a single animal by ID.
     */
    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    suspend fun getAnimalById(animalId: String): Animal?

    /**
     * Filters animals by species, only with AVAILABLE status.
     */
    @Query("SELECT * FROM animals WHERE species = :species AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun filterBySpeciesLocal(species: String): List<Animal>

    /**
     * Filters animals by size.
     */
    @Query("SELECT * FROM animals WHERE size = :size AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun filterBySizeLocal(size: String): List<Animal>

    /**
     * Sorts animals alphabetically ascending.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY name ASC")
    suspend fun sortByNameAscLocal(): List<Animal>

    /**
     * Sorts animals alphabetically descending.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY name DESC")
    suspend fun sortByNameDescLocal(): List<Animal>

    /**
     * Sorts animals by creation date.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun sortByDateLocal(): List<Animal>

    /**
     * Sorts animals by age ascending (oldest first).
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY birthDate ASC")
    suspend fun sortByAgeAscLocal(): List<Animal>

    /**
     * Sorts animals by age descending (youngest first).
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY birthDate DESC")
    suspend fun sortByAgeDescLocal(): List<Animal>

    /**
     * Searches animals by name.
     */
    @Query("SELECT * FROM animals WHERE name LIKE '%' || :query || '%' AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun searchAnimalsLocal(query: String): List<Animal>

    /**
     * Inserts or replaces a single animal.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(animal: Animal)

    /**
     * Inserts or replaces a list of animals.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<Animal>)

    /**
     * Deletes all animals in the table.
     */
    @Query("DELETE FROM animals")
    suspend fun deleteAll()

    /**
     * Updates the status of an animal.
     */
    @Query("UPDATE animals SET status = :status WHERE id = :animalId")
    suspend fun updateAnimalStatus(animalId: String, status: AnimalStatus)

    /**
     * Retrieves multiple animals by their IDs.
     * Used when syncing owned animals or loading activities.
     *
     * @param animalIds List of animal IDs to retrieve.
     * @return List of animals matching the provided IDs.
     */
    @Query("SELECT * FROM animals WHERE id IN (:animalIds)")
    suspend fun getAnimalsByIds(animalIds: List<String>): List<Animal>

    /**
     * Clears the table and inserts new data.
     * Used during synchronization with remote backend.
     */
    @Transaction
    suspend fun refreshCache(animals: List<Animal>) {
        deleteAll()
        insertAll(animals)
    }
}