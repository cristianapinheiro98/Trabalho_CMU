package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider.firestore
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
// MAPPER
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers() = userDao.getAllUsers()
    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)
    suspend fun getUserById(userId: String) = userDao.getUserById(userId)

    suspend fun updateUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))
            firestore.collection("users").document(user.id).update(user.toFirebaseMap()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}