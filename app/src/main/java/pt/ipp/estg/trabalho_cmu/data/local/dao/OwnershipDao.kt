package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

/**
 * Data Access Object (DAO) for the [Ownership] entity.
 * This interface provides methods for creating, reading, updating, and deleting
 * ownership requests from the `OwnershipRequests` table in the database.
 */
@Dao
interface OwnershipDao {

    /**
     * Retrieves all pending ownership requests submitted by a specific user.
     * The results are ordered by creation date, with the most recent requests first.
     *
     * @param userId The ID of the user whose ownership requests are to be retrieved.
     * @return A [LiveData] list of [Ownership] objects that automatically updates on data changes.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getOwnershipsByUser(userId: Int): LiveData<List<Ownership>>

    /**
     * Retrieves all pending ownership requests directed to a specific shelter.
     * The results are ordered by creation date, with the most recent requests first.
     *
     * @param shelterId The ID of the shelter whose ownership requests are to be retrieved.
     * @return A [LiveData] list of [Ownership] objects that automatically updates on data changes.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE shelterId = :shelterId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getOwnershipsByShelter(shelterId: Int): LiveData<List<Ownership>>


    /**
     * Fetches a single ownership request by its unique ID.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param id The unique ID of the ownership request.
     * @return The [Ownership] object if found, otherwise `null`.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE id = :id LIMIT 1")
    suspend fun getOwnershipById(id: Int): Ownership?


    /**
     * Inserts a single ownership request into the database.
     * If a request with the same primary key already exists, it will be replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param ownership The [Ownership] object to insert.
     * @return The row ID of the newly inserted request.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnership(ownership: Ownership) : Long

    /**
     * Inserts a list of ownership requests into the database.
     * If any request already exists, it will be replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param ownerships The list of [Ownership] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ownerships: List<Ownership>)

    /**
     * Updates the status of a specific ownership request.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param id The ID of the ownership request to update.
     * @param status The new [OwnershipStatus] to be set (e.g., 'APPROVED', 'REJECTED').
     */
    @Query("UPDATE OwnershipRequests SET status = :status WHERE id = :id")
    suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus)

    /**
     * Deletes a specific ownership request from the database.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param ownership The [Ownership] object to delete.
     */
    @Delete
    suspend fun deleteOwnership(ownership: Ownership)

    /**
     * Checks for an existing ownership request for a specific user and animal
     * that is currently in a 'PENDING' or 'APPROVED' state.
     * This is useful to prevent duplicate active requests.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param userId The ID of the user.
     * @param animalId The ID of the animal.
     * @return The existing [Ownership] object if one is found, otherwise `null`.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND animalId = :animalId AND status IN ('PENDING', 'APPROVED') LIMIT 1")
    suspend fun getExistingRequest(userId: Int, animalId: Int): Ownership?

}
