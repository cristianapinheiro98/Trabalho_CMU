package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

/**
 * Data Access Object (DAO) for the [User] entity.
 * This interface defines the methods for interacting with the `users` table
 * in the database, handling operations like querying, inserting, and deleting users.
 */
@Dao
interface UserDao {

    /**
     * Retrieves all users from the database, ordered by their ID in ascending order.
     *
     * @return A [LiveData] list of all [User] objects, which automatically
     *         notifies observers of any data changes.
     */
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): LiveData<List<User>>

    /**
     * Fetches a single user from the database based on their email address.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param email The email address of the user to retrieve.
     * @return The [User] object if a match is found, otherwise `null`.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Fetches a single user by their unique Firebase UID.
     * This is useful for linking local data with a remote Firebase authentication user.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param uid The Firebase Unique ID of the user.
     * @return The [User] object if found, otherwise `null`.
     */
    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getUserByFirebaseUid(uid: String): User?

    /**
     * Fetches a single user by their local database ID.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param userId The local ID of the user to retrieve.
     * @return The [User] object if found, otherwise `null`.
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    /**
     * Inserts a single user into the database. If a user with the same
     * primary key already exists, they will be replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param user The [User] object to insert.
     * @return The row ID of the newly inserted user.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User) : Long

    /**
     * Updates an existing user in the database.
     * The user is identified by their primary key.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param user The [User] object with updated information.
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * Deletes a specific user from the database.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param user The [User] object to be deleted.
     */
    @Delete
    suspend fun deleteUser(user: User)


    /**
     * Synchronously inserts a list of users.
     * This method is not a suspend function and is designed for use in contexts
     * where coroutines are not available, such as for seeding the database via a callback.
     *
     * @param users The list of [User] objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSync(users: List<User>)

}
