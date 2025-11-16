package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

@Dao
interface ShelterDao {
    @Query("SELECT * FROM shelters ORDER BY name ASC")
    fun getAllShelters(): LiveData<List<Shelter>>

    @Query("SELECT * FROM shelters ORDER BY name ASC")
    suspend fun getAllSheltersList(): List<Shelter>

    @Query("SELECT * FROM shelters WHERE id = :shelterId LIMIT 1")
    suspend fun getShelterById(shelterId: Int): Shelter?

    @Query("SELECT * FROM shelters WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getShelterByFirebaseUid(uid: String): Shelter?

    @Query("DELETE FROM shelters WHERE id = :shelterId")
    suspend fun deleteShelterById(shelterId: Int)

    @Query("SELECT * FROM shelters WHERE name LIKE :searchQuery ORDER BY name ASC")
    suspend fun searchSheltersByName(searchQuery: String): List<Shelter>

    @Query("SELECT COUNT(*) FROM shelters")
    suspend fun getShelterCount(): Int

    @Query("DELETE FROM shelters")
    suspend fun deleteAllShelters()

    @Query("SELECT * FROM shelters WHERE firebaseUid IS NULL")
    suspend fun getSheltersWithoutFirebaseUid(): List<Shelter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelter(shelter: Shelter) : Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelters(shelters: List<Shelter>)

    @Update
    suspend fun updateShelter(shelter: Shelter)

    @Delete
    suspend fun deleteShelter(shelter: Shelter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSync(shelters: List<Shelter>)
}
