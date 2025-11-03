package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

class AnimalRepository(private val animalDao: AnimalDao) {
    fun getAllAnimals() : LiveData<List<Animal>> = animalDao.getAllAnimals()
    suspend fun getAnimalById(animalId: String) = animalDao.getAnimalById(animalId)
    suspend fun insertAnimal(animal: Animal) = animalDao.insertAnimal(animal)
}
