package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

@Dao
interface AnimalDao {

    @Query("SELECT * FROM animals")
    fun getAllAnimals(): LiveData<List<Animal>>

    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    suspend fun getAnimalById(animalId: String): Animal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal)

    @Delete
    suspend fun deleteAnimal(animal: Animal)
}
