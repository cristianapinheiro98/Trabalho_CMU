package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.di.RetrofitInstance
import java.io.IOException

class AnimalRepository(private val animalDao: AnimalDao) {
    fun getAllAnimals() : LiveData<List<Animal>> = animalDao.getAllAnimals()
    suspend fun getAnimalById(animalId: String) = animalDao.getAnimalById(animalId)
    suspend fun insertAnimal(animal: Animal) = animalDao.insertAnimal(animal)
    suspend fun refreshAnimals(sortBy: String? = null, order: String? = null): List<Animal> {
        return withContext(Dispatchers.IO) {
            try {
                val remoteAnimals = RetrofitInstance.api.getAnimais(sortBy, order)
                remoteAnimals.forEach { animalDao.insertAnimal(it) }
                remoteAnimals
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

}


