package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.AnimalStatus

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun getAllAnimals(): LiveData<List<Animal>>

    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun getAllAnimalsNow(): List<Animal>

    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    suspend fun getAnimalById(animalId: Int): Animal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<Animal>)

    @Delete
    suspend fun deleteAnimal(animal: Animal)

    @Query("DELETE FROM animals")
    suspend fun clearAll()

    @Query("UPDATE animals SET status = :status WHERE id = :animalId")
    suspend fun updateAnimalStatus(animalId: Int, status: AnimalStatus)

    suspend fun updateAnimalToOwned(animalId: Int) {
        updateAnimalStatus(animalId, AnimalStatus.HASOWNED)
    }

}
