package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

/**
 * DAO responsible for local storage and retrieval of User entities.
 * Supports searching, inserting, updating, and cache refreshing.
 */
@Dao
interface UserDao {

    /**
     * Returns all users sorted alphabetically.
     */
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): LiveData<List<User>>

    /**
     * Retrieves a user by email.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Retrieves a user by ID.
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    /**
     * Inserts or replaces a user.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    /**
     * Inserts or replaces multiple users.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    /**
     * Deletes all users.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAll()

    /**
     * Refreshes local cache by clearing and reinserting users.
     */
    @Transaction
    suspend fun refreshCache(users: List<User>) {
        deleteAll()
        insertAll(users)
    }

    /**
     * Updates a user entity.
     */
    @Update
    suspend fun update(user: User)
}
