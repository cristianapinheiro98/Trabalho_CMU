package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers() = userDao.getAllUsers()

    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)

    suspend fun registerUser(user: User) = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}
