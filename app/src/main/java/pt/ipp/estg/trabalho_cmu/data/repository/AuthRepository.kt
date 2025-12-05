package pt.ipp.estg.trabalho_cmu.data.repository

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.LoginResult
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toShelter
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toUser
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils

class AuthRepository(
    private val userDao: UserDao,
    private val shelterDao: ShelterDao,
    private val application: Application
) {
    private val firebaseAuth = FirebaseProvider.auth
    private val firestore = FirebaseProvider.firestore

    // --- REGISTER USER (APENAS FIREBASE - SEM ROOM) ---
    suspend fun registerUser(
        name: String, address: String, phone: String, email: String, password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        // Verificação de rede aqui no Repositório
        if (!NetworkUtils.isConnected()) {
            return@withContext Result.failure(Exception("Registo requer conexão à internet."))
        }

        return@withContext try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Erro UID")

            val user = User(uid, name, address, email, phone)

            val userData = user.toFirebaseMap().toMutableMap()
            userData["accountType"] = "USER"

            firestore.collection("users").document(uid).set(userData).await()


            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REGISTER SHELTER (APENAS FIREBASE - SEM ROOM) ---
    suspend fun registerShelter(
        name: String, address: String, contact: String, email: String, password: String
    ): Result<Shelter> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) {
            return@withContext Result.failure(Exception("Registo requer conexão à internet."))
        }

        return@withContext try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Erro UID")

            val shelter = Shelter(uid, name, address, contact, email)

            val shelterData = shelter.toFirebaseMap().toMutableMap()
            shelterData["accountType"] = "SHELTER"

            firestore.collection("shelters").document(uid).set(shelterData).await()



            Result.success(shelter)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOGIN (FIREBASE -> COPIA PARA ROOM) ---
    suspend fun login(email: String, password: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) {
            return@withContext Result.failure(Exception("Login requer conexão à internet."))
        }

        return@withContext try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("UID não encontrado")

            // Tentar User
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toUser()!!.copy(id = uid)
                // SYNC: Copiar para Room (Cache de Leitura)
                userDao.insert(user)
                return@withContext Result.success(LoginResult(user = user, accountType = AccountType.USER))
            }

            // Tentar Shelter
            val shelterDoc = firestore.collection("shelters").document(uid).get().await()
            if (shelterDoc.exists()) {
                val shelter = shelterDoc.toShelter()!!.copy(id = uid)
                // SYNC: Copiar para Room (Cache de Leitura)
                shelterDao.insert(shelter)
                return@withContext Result.success(LoginResult(shelter = shelter, accountType = AccountType.SHELTER))
            }

            throw Exception("Conta não encontrada.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CHECK SESSION (AUTO LOGIN) ---
    suspend fun checkSession(): Result<LoginResult?> = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.success(null)

        // Se Offline: Usa o que tem no Room (Cache válida)
        if (!NetworkUtils.isConnected()) {
            val localUser = userDao.getUserById(uid)
            if (localUser != null) {
                return@withContext Result.success(LoginResult(user = localUser, accountType = AccountType.USER))
            }
            val localShelter = shelterDao.getShelterById(uid)
            if (localShelter != null) {
                return@withContext Result.success(LoginResult(shelter = localShelter, accountType = AccountType.SHELTER))
            }
            // Se não tem cache, obriga a login
            return@withContext Result.success(null)
        }

        // Se Online: Atualiza a Cache
        return@withContext try {
            firebaseAuth.currentUser?.getIdToken(true)?.await()

            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toUser()!!.copy(id = uid)
                userDao.insert(user) // Sync Room
                return@withContext Result.success(LoginResult(user = user, accountType = AccountType.USER))
            }

            val shelterDoc = firestore.collection("shelters").document(uid).get().await()
            if (shelterDoc.exists()) {
                val shelter = shelterDoc.toShelter()!!.copy(id = uid)
                shelterDao.insert(shelter) // Sync Room
                return@withContext Result.success(LoginResult(shelter = shelter, accountType = AccountType.SHELTER))
            }

            Result.success(null)
        } catch (e: Exception) {
            firebaseAuth.signOut()
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserId() = firebaseAuth.currentUser?.uid
}