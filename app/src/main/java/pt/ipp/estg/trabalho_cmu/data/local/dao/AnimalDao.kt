package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals")
    fun getAllAnimals(): LiveData<List<Animal>>

    @Query("SELECT * FROM animals")
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
}
