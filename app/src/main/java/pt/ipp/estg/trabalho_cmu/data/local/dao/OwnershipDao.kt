package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

/**
 * DAO for accessing and managing ownership requests.
 * LiveData automatically notifies observers when data changes.
 */
@Dao
interface OwnershipDao {
    @Query("SELECT * FROM OwnershipRequests WHERE userFirebaseUid = :userFirebaseUid AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getOwnershipsByUser(userFirebaseUid: String): LiveData<List<Ownership>>

    @Query("SELECT * FROM OwnershipRequests WHERE shelterFirebaseUid = :shelterFirebaseUid AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getOwnershipsByShelter(shelterFirebaseUid: String): LiveData<List<Ownership>>

    @Query("SELECT * FROM OwnershipRequests WHERE id = :id LIMIT 1")
    suspend fun getOwnershipById(id: Int): Ownership?

    @Query("SELECT * FROM OwnershipRequests WHERE firebaseUid IS NULL")
    suspend fun getOwnershipsWithoutFirebaseUid(): List<Ownership>

    @Query("SELECT * FROM OwnershipRequests WHERE userFirebaseUid = :userFirebaseUid AND animalFirebaseUid = :animalFirebaseUid AND status IN ('PENDING', 'APPROVED') LIMIT 1")
    suspend fun getExistingRequest(userFirebaseUid: String, animalFirebaseUid: String): Ownership?

    @Query("UPDATE OwnershipRequests SET status = :status WHERE id = :id")
    suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnership(ownership: Ownership) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ownerships: List<Ownership>)

    @Delete
    suspend fun deleteOwnership(ownership: Ownership)
}
