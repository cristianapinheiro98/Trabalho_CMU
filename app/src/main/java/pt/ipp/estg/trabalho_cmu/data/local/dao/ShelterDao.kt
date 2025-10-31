package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

@Dao
interface ShelterDao {

    @Query("SELECT * FROM shelters")
    fun getAllShelters(): LiveData<List<Shelter>>

    @Query("SELECT * FROM shelters WHERE id = :shelterId LIMIT 1")
    suspend fun getShelterById(shelterId: Int): Shelter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelter(shelter: Shelter)
}
