package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

class AnimalRepository(private val animalDao: AnimalDao,  private val firestore: FirebaseFirestore) {
    private var listenerRegistration: ListenerRegistration? = null

    fun getAllAnimals(): LiveData<List<Animal>> = animalDao.getAllAnimals()
    suspend fun getAnimalById(animalId: Int) = animalDao.getAnimalById(animalId)

    suspend fun insertAnimal(animal: Animal) = animalDao.insertAnimal(animal)

    suspend fun createAnimal(animal: Animal): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val generatedId = animalDao.insertAnimal(animal).toInt()

            val animalWithId = animal.copy(id = generatedId)

            val data = hashMapOf(
                "id" to generatedId,
                "name" to animalWithId.name,
                "breed" to animalWithId.breed,
                "species" to animalWithId.species,
                "size" to animalWithId.size,
                "birthDate" to animalWithId.birthDate,
                "imageUrls" to animalWithId.imageUrls,
                "description" to animalWithId.description,
                "shelterId" to animalWithId.shelterId,
                "status" to animalWithId.status.name,
                "createdAt" to animalWithId.createdAt
            )

            firestore.collection("animals")
                .document(generatedId.toString())
                .set(data)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAnimals() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("animals").get().await()

            val animals = snapshot.documents.mapNotNull { it.toAnimal() }

            if (animals.isNotEmpty()) {
                animalDao.clearAll()
                animalDao.insertAll(animals)
            } else {
                println("Firebase is empty: keep local data")
            }


        } catch (e: Exception) {
            println("Error in getting data from firebase: ${e.message}")
            println("Keeping local data (Room)")
        }
    }




    suspend fun filterBySpecies(species: String): List<Animal> =
        animalDao.filterBySpeciesLocal(species)

    suspend fun filterBySize(size: String): List<Animal> =
        animalDao.filterBySizeLocal(size)

    suspend fun sortByName(): List<Animal> =
        animalDao.sortByNameLocal()

    suspend fun sortByAge(): List<Animal> =
        animalDao.sortByAgeLocal()

    suspend fun sortByDate(): List<Animal> =
        animalDao.sortByDateLocal()


    suspend fun changeAnimalStatusToOwned(animalId: Int) = withContext(Dispatchers.IO) {
        try {
            animalDao.updateAnimalToOwned(animalId)

            firestore.collection("animals")
                .document(animalId.toString())
                .update("status", AnimalStatus.HASOWNED.name)
                .await()

        } catch (e: Exception) {
            println("Error updating animal status: ${e.message}")
            throw e
        }
    }
    fun startFirebaseListener() {
        listenerRegistration?.remove()

        listenerRegistration = firestore.collection("animals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Firestore Listener Error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val animals = snapshot.documents.mapNotNull { it.toAnimal() }

                    if (animals.isEmpty()) {
                        println("Listener Firebase is empty, do not clean room")
                        return@addSnapshotListener
                    }

                    GlobalScope.launch(Dispatchers.IO) {
                        animalDao.clearAll()
                        animalDao.insertAll(animals)
                    }
                }
            }
    }


    fun stopListener() {
        listenerRegistration?.remove()
    }

    fun DocumentSnapshot.toAnimal(): Animal? = try {
        Animal(
            id = (getLong("id") ?: 0).toInt(),
            name = getString("name") ?: "",
            breed = getString("breed") ?: "",
            species = getString("species") ?: "",
            size = getString("size") ?: "",
            birthDate = getString("birthDate") ?: "",
            description = getString("description") ?: "",
            imageUrls = get("imageUrls") as? List<String> ?: emptyList(),
            shelterId = (getLong("shelterId") ?: 0).toInt(),
            createdAt = getLong("createdAt") ?: 0L,
            status = AnimalStatus.valueOf(getString("status") ?: "AVAILABLE")
        )
    } catch (e: Exception) {
        null
    }
}
