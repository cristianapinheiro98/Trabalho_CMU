package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

/**
 * DAO responsible for handling all ownership/adoption requests stored locally.
 * Supports querying by user, by shelter, inserting, updating, and cache refresh.
 */
@Dao
interface OwnershipDao {

    /**
     * Returns pending ownership requests made by a specific user.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingOwnershipsByUser(userId: String): LiveData<List<Ownership>>

    /**
     * Returns pending ownership requests received by a shelter.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE shelterId = :shelterId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingOwnershipsByShelter(shelterId: String): LiveData<List<Ownership>>

    /**
     * Retrieves a single ownership request by ID.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE id = :id LIMIT 1")
    suspend fun getOwnershipById(id: String): Ownership?

    /**
     * Checks if a user already has a pending or approved request for a specific animal.
     * Prevents duplicate requests.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND animalId = :animalId AND status IN ('PENDING', 'APPROVED') LIMIT 1")
    suspend fun getExistingRequest(userId: String, animalId: String): Ownership?

    /**
     * Inserts or replaces a single ownership request.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ownership: Ownership)

    /**
     * Inserts or replaces multiple ownership requests.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ownerships: List<Ownership>)

    /**
     * Deletes all ownership records.
     */
    @Query("DELETE FROM OwnershipRequests")
    suspend fun deleteAll()

    /**
     * Updates the status of a specific ownership request.
     */
    @Query("UPDATE OwnershipRequests SET status = :status WHERE id = :id")
    suspend fun updateOwnershipStatus(id: String, status: OwnershipStatus)

    /**
     * Completely refreshes the ownership cache (delete all + insert all).
     */
    @Transaction
    suspend fun refreshCache(ownerships: List<Ownership>) {
        deleteAll()
        insertAll(ownerships)
    }

    /**
     * Retrieves all approved ownerships for a specific user.
     */
    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND status = 'APPROVED'")
    suspend fun getApprovedOwnershipsByUser(userId: String): List<Ownership>
}
