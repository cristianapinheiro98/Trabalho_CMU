package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.remote.RetrofitInstance
import java.io.IOException

class AnimalRepository(private val animalDao: AnimalDao) {
    fun getAllAnimals(): LiveData<List<Animal>> = animalDao.getAllAnimals()
    suspend fun getAnimalById(animalId: Int) = animalDao.getAnimalById(animalId)

    suspend fun insertAnimal(animal: Animal) = animalDao.insertAnimal(animal)

    suspend fun fetchAnimals(): List<Animal> = withContext(Dispatchers.IO) {
        try {
            val remoteAnimals = RetrofitInstance.animalApi.getAnimais()
            animalDao.clearAll()
            animalDao.insertAll(remoteAnimals)
            remoteAnimals
        } catch (e: IOException) {
            e.printStackTrace()
            animalDao.getAllAnimalsNow()
        }
    }
    suspend fun filterBySpecies(species: String): List<Animal> = withContext(Dispatchers.IO) {
        try {
            val remote = RetrofitInstance.animalApi.getAnimais(species = species)
            animalDao.clearAll()
            animalDao.insertAll(remote)
            remote
        } catch (e: IOException) {
            animalDao.getAllAnimalsNow()
        }
    }

    suspend fun filterBySize(size: String): List<Animal> = withContext(Dispatchers.IO) {
        try {
            val remote = RetrofitInstance.animalApi.getAnimais(size = size)
            animalDao.clearAll()
            animalDao.insertAll(remote)
            remote
        } catch (e: IOException) {
            animalDao.getAllAnimalsNow()
        }
    }
    suspend fun filterByGender(gender: String): List<Animal> = withContext(Dispatchers.IO) {
        try {
            val remote = RetrofitInstance.animalApi.getAnimais(gender = gender)
            animalDao.clearAll()
            animalDao.insertAll(remote)
            remote
        } catch (e: IOException) {
            animalDao.getAllAnimalsNow()
        }
    }
    suspend fun sortByName(order: String = "asc"): List<Animal> = withContext(Dispatchers.IO) {
        try {
            val remote = RetrofitInstance.animalApi.getAnimais(sortBy = "name", order = order)
            animalDao.clearAll()
            animalDao.insertAll(remote)
            remote
        } catch (e: IOException) {
            animalDao.getAllAnimalsNow()
        }
    }
    suspend fun sortByAge(order: String = "asc"): List<Animal> = withContext(Dispatchers.IO) {
        try {
            val remote = RetrofitInstance.animalApi.getAnimais(sortBy = "age", order = order)
            animalDao.clearAll()
            animalDao.insertAll(remote)
            remote
        } catch (e: IOException) {
            animalDao.getAllAnimalsNow()
        }
    }
    suspend fun sortByDate(order: String = "desc"): List<Animal> = withContext(Dispatchers.IO) {
        try {
            val remote = RetrofitInstance.animalApi.getAnimais(sortBy = "createdAt", order = order)
            animalDao.clearAll()
            animalDao.insertAll(remote)
            remote
        } catch (e: IOException) {
            animalDao.getAllAnimalsNow()
        }
    }


    suspend fun changeAnimalStatusToOwned(animalId: Int) {
        animalDao.updateAnimalToOwned(animalId)
    }
}
