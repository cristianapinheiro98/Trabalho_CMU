package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider.firestore
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap

/**
 * Repository responsible for:
 *  - Accessing user profiles stored in Room
 *  - Fetching users by ID or email
 *  - Updating Firestore user profiles (online only)
 *
 * This repository depends on Context to retrieve localized error strings.
 */
class UserRepository(
    private val appContext: Context,
    private val userDao: UserDao
) {

    /** Returns all users stored in Room as LiveData. */
    fun getAllUsers() = userDao.getAllUsers()

    /** Retrieves a user from Room by email. */
    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)

    /** Retrieves a user from Room by ID. */
    suspend fun getUserById(userId: String) = userDao.getUserById(userId)

    /**
     * Updates a user profile in Firestore.
     *
     * Fails if offline. Error strings come from the localized resources.
     */
    suspend fun updateUser(user: User): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isConnected()) {
                    val msg = appContext.getString(R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                firestore.collection("users")
                    .document(user.id)
                    .update(user.toFirebaseMap())
                    .await()

                Result.success(Unit)

            } catch (e: Exception) {
                val msg = appContext.getString(R.string.error_update_user)
                Result.failure(Exception(msg))
            }
        }
}
