package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.OwnershipRequest
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus

/**
 * DAO for accessing and managing ownership requests.
 * LiveData automatically notifies observers when data changes.
 */
@Dao
interface OnwershipRequestDao {

    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOwnershipsByUser(userId: String): LiveData<List<OwnershipRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnership(ownership: OwnershipRequest)

    @Query("UPDATE OwnershipRequests SET status = :status WHERE id = :id")
    suspend fun updateOwnershipStatus(id: Int, status: OwnershipStatus)

    @Delete
    suspend fun deleteOwnership(ownership: OwnershipRequest)
}
