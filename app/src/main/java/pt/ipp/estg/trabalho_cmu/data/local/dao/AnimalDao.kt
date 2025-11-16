package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun getAllAnimals(): LiveData<List<Animal>>


    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    suspend fun getAnimalById(animalId: Int): Animal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<Animal>)

    //No AppDatabase, quando se faz a seed, o callback corre fora de coroutine, por isso não se pode chamar funções suspend
    //used in seed
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSync(animals: List<Animal>)

    @Delete
    suspend fun deleteAnimal(animal: Animal)

    @Query("DELETE FROM animals")
    suspend fun clearAll()

    @Query("UPDATE animals SET status = :status WHERE id = :animalId")
    suspend fun updateAnimalStatus(animalId: Int, status: AnimalStatus)

    suspend fun updateAnimalToOwned(animalId: Int) {
        updateAnimalStatus(animalId, AnimalStatus.HASOWNED)
    }

    //filters

    @Query("SELECT * FROM animals WHERE species = :species AND status = 'AVAILABLE'")
    suspend fun filterBySpeciesLocal(species: String): List<Animal>

    @Query("SELECT * FROM animals WHERE size = :size AND status = 'AVAILABLE'")
    suspend fun filterBySizeLocal(size: String): List<Animal>

   //ordering

    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY name ASC")
    suspend fun sortByNameLocal(): List<Animal>

    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun sortByDateLocal(): List<Animal>

    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY birthDate ASC")
    suspend fun sortByAgeLocal(): List<Animal>

    //search
    @Query("SELECT * FROM animals WHERE name LIKE '%' || :query || '%' AND status = 'AVAILABLE'")
    suspend fun searchAnimalsLocal(query: String): List<Animal>

    @Query("SELECT * FROM animals WHERE firebaseUid IS NULL")
    suspend fun getAnimalsWithoutFirebaseUid(): List<Animal>

    @Query("SELECT * FROM animals WHERE firebaseUid = :firebaseUid LIMIT 1")
    suspend fun getAnimalByFirebaseUid(firebaseUid: String): Animal?

    @Update
    suspend fun updateAnimal(animal: Animal)
}
