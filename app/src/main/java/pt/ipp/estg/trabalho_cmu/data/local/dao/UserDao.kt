package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    // Atualizar cache de utilizadores (para admins, por exemplo)
    @Transaction
    suspend fun refreshCache(users: List<User>) {
        deleteAll()
        insertAll(users)
    }

    @Update
    suspend fun update(user: User)
}