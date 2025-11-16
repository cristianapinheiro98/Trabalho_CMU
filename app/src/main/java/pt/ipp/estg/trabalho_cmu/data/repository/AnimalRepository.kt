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
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider

class AnimalRepository(private val animalDao: AnimalDao) {
    private val firestore = FirebaseProvider.firestore
    private var listenerRegistration: ListenerRegistration? = null

    fun getAllAnimals(): LiveData<List<Animal>> = animalDao.getAllAnimals()

    suspend fun getAnimalById(animalId: Int) = animalDao.getAnimalById(animalId)

    suspend fun getAnimalByFirebaseUid(firebaseUid: String) = animalDao.getAnimalByFirebaseUid(firebaseUid)

    suspend fun createAnimal(animal: Animal): Result<Animal> = withContext(Dispatchers.IO) {
        try {
            // Create in Room (offline-first)
            val roomId = animalDao.insertAnimal(animal).toInt()
            val animalWithRoomId = animal.copy(id = roomId)

            // Try Firebase (if online)
            try {
                val data = hashMapOf(
                    "id" to roomId,
                    "name" to animalWithRoomId.name,
                    "breed" to animalWithRoomId.breed,
                    "species" to animalWithRoomId.species,
                    "size" to animalWithRoomId.size,
                    "birthDate" to animalWithRoomId.birthDate,
                    "imageUrls" to animalWithRoomId.imageUrls,
                    "description" to animalWithRoomId.description,
                    "shelterFirebaseUid" to animalWithRoomId.shelterFirebaseUid,
                    "status" to animalWithRoomId.status.name,
                    "createdAt" to animalWithRoomId.createdAt
                )

                val docRef = firestore.collection("animals").add(data).await()
                val firebaseUid = docRef.id

                // Update Room with firebaseUid
                val finalAnimal = animalWithRoomId.copy(firebaseUid = firebaseUid)
                animalDao.updateAnimal(finalAnimal)

                Result.success(finalAnimal)
            } catch (e: Exception) {
                println("Firebase offline, animal saved locally only")
                Result.success(animalWithRoomId)
            }
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

            val animal = animalDao.getAnimalById(animalId)
            val firebaseUid = animal?.firebaseUid

            if (firebaseUid != null) {
                firestore.collection("animals")
                    .document(firebaseUid)
                    .update("status", AnimalStatus.HASOWNED.name)
                    .await()
            }
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
            firebaseUid = id,
            name = getString("name") ?: "",
            breed = getString("breed") ?: "",
            species = getString("species") ?: "",
            size = getString("size") ?: "",
            birthDate = getString("birthDate") ?: "",
            description = getString("description") ?: "",
            imageUrls = get("imageUrls") as? List<String> ?: emptyList(),
            shelterFirebaseUid = getString("shelterFirebaseUid") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            status = AnimalStatus.valueOf(getString("status") ?: "AVAILABLE")
        )
    } catch (e: Exception) {
        null
    }

    suspend fun syncPendingAnimals() = withContext(Dispatchers.IO) {
        try {
            val pending = animalDao.getAnimalsWithoutFirebaseUid()
            pending.forEach { animal ->
                try {
                    val data = hashMapOf(
                        "id" to animal.id,
                        "name" to animal.name,
                        "breed" to animal.breed,
                        "species" to animal.species,
                        "size" to animal.size,
                        "birthDate" to animal.birthDate,
                        "imageUrls" to animal.imageUrls,
                        "description" to animal.description,
                        "shelterFirebaseUid" to animal.shelterFirebaseUid,
                        "status" to animal.status.name,
                        "createdAt" to animal.createdAt
                    )
                    val docRef = firestore.collection("animals").add(data).await()
                    animalDao.updateAnimal(animal.copy(firebaseUid = docRef.id))
                } catch (e: Exception) {
                    println("Failed to sync animal ${animal.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error syncing pending animals: ${e.message}")
        }
    }
}
