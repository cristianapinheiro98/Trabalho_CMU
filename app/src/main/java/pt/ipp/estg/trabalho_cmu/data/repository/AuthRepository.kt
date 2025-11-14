package pt.ipp.estg.trabalho_cmu.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.data.models.LoginResult

class AuthRepository(
    private val userDao: UserDao,
    private val shelterDao: ShelterDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun registerUser(
        name: String,
        address: String,
        phone: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            // Create in Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUid = authResult.user?.uid ?: throw Exception("Error creating user in Auth")

            // Save user in Firestore
            val userData = hashMapOf(
                "name" to name,
                "address" to address,
                "phone" to phone,
                "email" to email,
                "accountType" to "USER"
            )
            firestore.collection("users").document(firebaseUid).set(userData).await()

            // Save user in Room
            val user = User(
                firebaseUid = firebaseUid,
                name = name,
                adress = address,
                phone = phone,
                email = email,
                password = "" // password is not stored locally
            )
            val generatedId = userDao.insertUser(user).toInt()
            val userWithId = user.copy(id = generatedId)

            Result.success(userWithId)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerShelter(
        name: String,
        address: String,
        contact: String,
        email: String,
        password: String
    ): Result<Shelter> {
        return try {
            // Create in Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUid = authResult.user?.uid ?: throw Exception("Error creating shelter in Auth")

            // Save shelter in Firestore
            val shelterData = hashMapOf(
                "name" to name,
                "address" to address,
                "contact" to contact,
                "email" to email,
                "accountType" to "SHELTER"
            )
            firestore.collection("shelters").document(firebaseUid).set(shelterData).await()

            // Save shelter in Room
            val shelter = Shelter(
                firebaseUid = firebaseUid,
                name = name,
                address = address,
                phone = contact,
                email = email,
                password = "" // password is not stored locally
            )
            val generatedId = shelterDao.insertShelter(shelter).toInt()

            val shelterWithId = shelter.copy(id = generatedId)

            Result.success(shelterWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== LOGIN ONLINE =====

    suspend fun login(email: String, password: String): Result<LoginResult> {
        return try {
            // Authenticate through Firebase Auth
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("UID not found")

            // Try as user
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = User(
                    firebaseUid = uid,
                    name = userDoc.getString("name") ?: "",
                    adress = userDoc.getString("address") ?: "",
                    phone = userDoc.getString("phone") ?: "",
                    email = userDoc.getString("email") ?: "",
                    password = ""
                )
                // Room cache
                val generatedId = userDao.insertUser(user).toInt()
                val userWithId = user.copy(id = generatedId)
                return Result.success(LoginResult(user = userWithId, accountType = AccountType.USER))
            }

            // Try as Shelter
            val shelterDoc = firestore.collection("shelters").document(uid).get().await()
            if (shelterDoc.exists()) {
                val shelter = Shelter(
                    firebaseUid = uid,
                    name = shelterDoc.getString("name") ?: "",
                    address = shelterDoc.getString("address") ?: "",
                    phone = shelterDoc.getString("contact") ?: "",
                    email = userDoc.getString("email") ?: "",
                    password = ""
                )
                // Room cache
                val generatedId = shelterDao.insertShelter(shelter).toInt()
                val shelterWithId = shelter.copy(id = generatedId)
                return Result.success(LoginResult(shelter = shelterWithId, accountType = AccountType.SHELTER))
            }

            throw Exception("Account not found")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== LOGIN OFFLINE =====

    suspend fun checkOfflineSession(): LoginResult? {
        val uid = firebaseAuth.currentUser?.uid ?: return null

        // Get user from cache
        val user = userDao.getUserByFirebaseUid(uid)
        if (user != null) {
            return LoginResult(user = user, accountType = AccountType.USER)
        }

        // Get shelter from cache
        val shelter = shelterDao.getShelterByFirebaseUid(uid)
        if (shelter != null) {
            return LoginResult(shelter = shelter, accountType = AccountType.SHELTER)
        }

        return null
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    // ===== HELPERS =====

    fun isAuthenticated() = firebaseAuth.currentUser != null

    fun getCurrentFirebaseUid() = firebaseAuth.currentUser?.uid
}