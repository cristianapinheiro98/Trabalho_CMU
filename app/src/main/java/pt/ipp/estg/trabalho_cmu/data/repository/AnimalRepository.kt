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
    suspend fun getAnimalById(animalId: Int) = animalDao.getAnimalById(animalId)
    suspend fun insertAnimal(animal: Animal) = animalDao.insertAnimal(animal)

    /*suspend fun refreshAnimals(
        sortBy: String? = null,
        order: String? = null
    ): List<Animal> = withContext(Dispatchers.IO) {
        try {
            // Vai buscar dados remotos
            val remoteAnimals = RetrofitInstance.api.getAnimais(sortBy, order)

            // 🔹 2. Atualiza a base de dados local de forma eficiente
            animalDao.clearAll() // opcional, se quiseres substituir tudo
            animalDao.insertAll(remoteAnimals)

            // 🔹 3. Retorna lista atualizada
            remoteAnimals
        } catch (e: IOException) {
            e.printStackTrace()
            // 🔹 4. Em caso de erro de rede, retorna dados locais
            animalDao.getAllAnimals()
        } as List<Animal>
    }*/


}


