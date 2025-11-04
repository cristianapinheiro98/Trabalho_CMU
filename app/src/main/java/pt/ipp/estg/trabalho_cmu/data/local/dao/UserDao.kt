package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

@Dao
interface UserDao {

    // Obter todos os utilizadores (ex: debug)
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): LiveData<List<User>>

    // Procurar utilizador por email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // Inserir (usado no registo)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Atualizar perfil
    @Update
    suspend fun updateUser(user: User)

    // Eliminar (opcional)
    @Delete
    suspend fun deleteUser(user: User)
}
