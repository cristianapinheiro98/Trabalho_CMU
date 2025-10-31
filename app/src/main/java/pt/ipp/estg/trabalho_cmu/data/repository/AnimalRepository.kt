package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import javax.inject.Inject

class AnimalRepository @Inject constructor(
    private val animalDao: AnimalDao
) {
    fun getAllAnimals() = animalDao.getAllAnimals()
    suspend fun getAnimalById(animalId: String) = animalDao.getAnimalById(animalId)
    suspend fun insertAnimal(animal: Animal) = animalDao.insertAnimal(animal)
}

