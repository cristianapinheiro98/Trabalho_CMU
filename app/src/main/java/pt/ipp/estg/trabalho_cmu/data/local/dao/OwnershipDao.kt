package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus

@Dao
interface OwnershipDao {
    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingOwnershipsByUser(userId: String): LiveData<List<Ownership>>

    @Query("SELECT * FROM OwnershipRequests WHERE shelterId = :shelterId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingOwnershipsByShelter(shelterId: String): LiveData<List<Ownership>>

    @Query("SELECT * FROM OwnershipRequests WHERE id = :id LIMIT 1")
    suspend fun getOwnershipById(id: String): Ownership?

    // Verifica duplicados (usado antes de criar)
    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND animalId = :animalId AND status IN ('PENDING', 'APPROVED') LIMIT 1")
    suspend fun getExistingRequest(userId: String, animalId: String): Ownership?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ownership: Ownership)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ownerships: List<Ownership>)

    @Query("DELETE FROM OwnershipRequests")
    suspend fun deleteAll()

    // --- MÉTODOS EM FALTA ADICIONADOS ---

    // 1. Atualizar status (Aprovar/Rejeitar)
    @Query("UPDATE OwnershipRequests SET status = :status WHERE id = :id")
    suspend fun updateOwnershipStatus(id: String, status: OwnershipStatus)

    // 2. Refresh Cache
    @Transaction
    suspend fun refreshCache(ownerships: List<Ownership>) {
        deleteAll()
        insertAll(ownerships)
    }

    @Query("SELECT * FROM OwnershipRequests WHERE userId = :userId AND status = 'APPROVED'")
    suspend fun getApprovedOwnershipsByUser(userId: String): List<Ownership>
}