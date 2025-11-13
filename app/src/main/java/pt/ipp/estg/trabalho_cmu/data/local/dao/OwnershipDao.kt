package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus

/**
 * DAO for accessing and managing ownership requests.
 * LiveData automatically notifies observers when data changes.
 */
@Dao
interface OwnershipDao {

    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getOwnershipsByUser(userId: Int): LiveData<List<Ownership>>

    @Query("SELECT * FROM OwnershipRequests WHERE shelterId = :shelterId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getOwnershipsByShelter(shelterId: Int): LiveData<List<Ownership>>

    @Query("SELECT * FROM OwnershipRequests WHERE id = :id LIMIT 1")
    suspend fun getOwnershipById(id: Int): Ownership?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnership(ownership: Ownership)

    @Query("UPDATE OwnershipRequests SET status = :status WHERE id = :id")
    suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus)

    @Delete
    suspend fun deleteOwnership(ownership: Ownership)

}
