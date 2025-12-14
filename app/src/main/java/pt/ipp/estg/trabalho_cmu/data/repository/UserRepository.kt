package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider.firestore
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toUser
import pt.ipp.estg.trabalho_cmu.utils.StringHelper

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
                    val msg = StringHelper.getString(appContext, R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                firestore.collection("users")
                    .document(user.id)
                    .update(user.toFirebaseMap())
                    .await()

                Result.success(Unit)

            } catch (e: Exception) {
                val msg = StringHelper.getString(appContext, R.string.error_update_user)
                Result.failure(Exception(msg))
            }
        }

    /**
     * Fetches a specific user from Firestore and saves it to Room.
     * Used when viewing adoption requests for users not yet in the local DB.
     */
    suspend fun syncSpecificUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) {
            val msg = StringHelper.getString(appContext, R.string.error_offline)
            return@withContext Result.failure(Exception(msg))
        }

        try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = document.toUser()

                if (user != null) {
                    userDao.insert(user)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(StringHelper.getString(appContext, R.string.error_convert_user)))
                }
            } else {
                Result.failure(Exception(StringHelper.getString(appContext, R.string.error_user_not_found)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(StringHelper.getString(appContext, R.string.error_sync_user_generic)))
        }
    }

    /**
     * Fetches all users from Firestore and saves it to Room.
     * Used when viewing adoption requests for users not yet in the local DB.
     */
    suspend fun syncUsers(): Result<Unit> = withContext(Dispatchers.IO) {

        if (!NetworkUtils.isConnected()) {
            val msg = StringHelper.getString(appContext, R.string.error_offline)
            return@withContext Result.failure(Exception(msg))
        }

        try {

            val snapshot = firestore.collection("users").get().await()

            val usersList = snapshot.documents.mapNotNull { doc ->
                val user = doc.toUser()
                user
            }

            if (usersList.isNotEmpty()) {
                userDao.insertAll(usersList)
            }

            Result.success(Unit)

        } catch (e: Exception) {
            val msg = StringHelper.getString(appContext, R.string.error_sync_users)
            Result.failure(Exception(msg))
        }
    }
}