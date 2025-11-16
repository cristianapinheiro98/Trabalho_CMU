package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.User

class UserRepository(private val userDao: UserDao) {

    fun getAllUsers() = userDao.getAllUsers()

    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)

    suspend fun getUserById(userId: Int) = userDao.getUserById(userId)

    suspend fun getUserByFirebaseUid(uid: String) = userDao.getUserByFirebaseUid(uid)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}