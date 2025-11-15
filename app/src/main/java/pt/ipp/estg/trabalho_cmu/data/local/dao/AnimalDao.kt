package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

/**
 * Data Access Object (DAO) for the [Animal] entity.
 * This interface defines the database interactions for the `animals` table,
 * providing methods for querying, inserting, updating, and deleting animal data.
 */
@Dao
interface AnimalDao {

    /**
     * Retrieves all animals with a status of 'AVAILABLE' from the database,
     * ordered by their creation date in descending order (newest first).
     *
     * @return A [LiveData] list of [Animal] objects, which automatically updates the UI on data changes.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun getAllAnimals(): LiveData<List<Animal>>

    /**
     * Fetches a single animal by its unique ID.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param animalId The ID of the animal to retrieve.
     * @return The [Animal] object if found, otherwise `null`.
     */
    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    suspend fun getAnimalById(animalId: Int): Animal?

    /**
     * Inserts a single animal into the database. If the animal already exists, it is replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param animal The [Animal] to be inserted.
     * @return The row ID of the newly inserted animal.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal) : Long

    /**
     * Inserts a list of animals into the database. If any animal already exists, it is replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param animals The list of [Animal] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<Animal>)

    /**
     * Synchronously inserts a list of animals.
     * This method is not a suspend function and is intended for use in contexts
     * where coroutines are not available, such as a Room database callback for seeding data.
     *
     * @param animals The list of [Animal] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSync(animals: List<Animal>)

    /**
     * Deletes a specific animal from the database.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param animal The [Animal] to be deleted.
     */
    @Delete
    suspend fun deleteAnimal(animal: Animal)

    /**
     * Deletes all records from the `animals` table.
     * This is a suspend function and must be called from a coroutine.
     */
    @Query("DELETE FROM animals")
    suspend fun clearAll()

    /**
     * Updates the status of a specific animal.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param animalId The ID of the animal to update.
     * @param status The new [AnimalStatus] to set.
     */
    @Query("UPDATE animals SET status = :status WHERE id = :animalId")
    suspend fun updateAnimalStatus(animalId: Int, status: AnimalStatus)

    /**
    * A convenience function to update an animal's status to 'HASOWNED'.
    * This is a suspend function and must be called from a coroutine.
    *
    * @param animalId The ID of the animal to mark as owned.
    */
    suspend fun updateAnimalToOwned(animalId: Int) {
        updateAnimalStatus(animalId, AnimalStatus.HASOWNED)
    }

    /**
     * Filters available animals by their species.
     *
     * @param species The species to filter by (e.g., "Cat", "Dog").
     * @return A list of available [Animal] objects matching the species.
     */
    @Query("SELECT * FROM animals WHERE species = :species AND status = 'AVAILABLE'")
    suspend fun filterBySpeciesLocal(species: String): List<Animal>

    /**
     * Filters available animals by their size.
     *
     * @param size The size to filter by (e.g., "Small", "Medium", "Large").
     * @return A list of available [Animal] objects matching the size.
     */
    @Query("SELECT * FROM animals WHERE size = :size AND status = 'AVAILABLE'")
    suspend fun filterBySizeLocal(size: String): List<Animal>

    /**
     * Sorts all available animals by name in alphabetical (ascending) order.
     *
     * @return A sorted list of available [Animal] objects.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY name ASC")
    suspend fun sortByNameLocal(): List<Animal>


    /**
     * Sorts all available animals by their creation date in descending order (newest first).
     *
     * @return A sorted list of available [Animal] objects.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun sortByDateLocal(): List<Animal>

    /**
     * Sorts all available animals by their birth date in ascending order (oldest first).
     *
     * @return A sorted list of available [Animal] objects.
     */
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY birthDate ASC")
    suspend fun sortByAgeLocal(): List<Animal>

    /**
     * Searches for available animals whose name contains the given query string.
     * The search is case-insensitive.
     *
     * @param query The text to search for within the animal names.
     * @return A list of [Animal] objects that match the search query.
     */
    @Query("SELECT * FROM animals WHERE name LIKE '%' || :query || '%' AND status = 'AVAILABLE'")
    suspend fun searchAnimalsLocal(query: String): List<Animal>

}
