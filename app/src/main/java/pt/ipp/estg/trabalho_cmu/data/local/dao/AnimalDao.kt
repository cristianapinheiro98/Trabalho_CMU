package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

@Dao
interface AnimalDao {
    // LEITURA
    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun getAllAnimals(): LiveData<List<Animal>>

    @Query("SELECT * FROM animals")
    suspend fun getAllAnimalsList(): List<Animal>

    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    suspend fun getAnimalById(animalId: String): Animal?

    // FILTROS & PESQUISA
    @Query("SELECT * FROM animals WHERE species = :species AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun filterBySpeciesLocal(species: String): List<Animal>

    @Query("SELECT * FROM animals WHERE size = :size AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun filterBySizeLocal(size: String): List<Animal>

    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY name ASC")
    suspend fun sortByNameLocal(): List<Animal>

    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun sortByDateLocal(): List<Animal>

    @Query("SELECT * FROM animals WHERE status = 'AVAILABLE' ORDER BY birthDate ASC")
    suspend fun sortByAgeLocal(): List<Animal>

    @Query("SELECT * FROM animals WHERE name LIKE '%' || :query || '%' AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun searchAnimalsLocal(query: String): List<Animal>

    // ESCRITA (Cache)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(animal: Animal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<Animal>)

    @Query("DELETE FROM animals")
    suspend fun deleteAll()

    // --- MÉTODOS EM FALTA ADICIONADOS ---

    // 1. Atualizar status (usado ao aceitar adoção)
    @Query("UPDATE animals SET status = :status WHERE id = :animalId")
    suspend fun updateAnimalStatus(animalId: String, status: AnimalStatus)

    // 2. Atualizar cache completa (Atomicamente)
    @Transaction
    suspend fun refreshCache(animals: List<Animal>) {
        deleteAll()
        insertAll(animals)
    }
}