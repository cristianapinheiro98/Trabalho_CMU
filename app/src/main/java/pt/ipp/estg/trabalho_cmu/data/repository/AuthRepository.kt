package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.LoginResult
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toShelter
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toUser
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils

/**
 * Authentication repository responsible for:
 * - Registering users and shelters
 * - Logging in accounts
 * - Storing profiles in Firebase
 * - Syncing data to Room
 * - Restoring authenticated sessions
 *
 * This repository depends on Context to fetch localized error strings.
 */
class AuthRepository(
    private val appContext: Context,
    private val userDao: UserDao,
    private val shelterDao: ShelterDao
) {

    private val firebaseAuth = FirebaseProvider.auth
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore

    // -----------------------------------------------------------
    // REGISTER USER
    // -----------------------------------------------------------
    suspend fun registerUser(
        name: String,
        address: String,
        phone: String,
        email: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {

        if (!NetworkUtils.isConnected()) {
            return@withContext Result.failure(
                Exception(appContext.getString(R.string.error_register_requires_internet))
            )
        }

        return@withContext try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val uid = authResult.user?.uid
                ?: throw Exception(appContext.getString(R.string.error_uid))

            val user = User(uid, name, address, email, phone)
            val userData = user.toFirebaseMap().toMutableMap()
            userData["accountType"] = "USER"

            firestore.collection("users")
                .document(uid)
                .set(userData)
                .await()

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -----------------------------------------------------------
    // REGISTER SHELTER
    // -----------------------------------------------------------
    suspend fun registerShelter(
        name: String,
        address: String,
        contact: String,
        email: String,
        password: String
    ): Result<Shelter> = withContext(Dispatchers.IO) {

        if (!NetworkUtils.isConnected()) {
            return@withContext Result.failure(
                Exception(appContext.getString(R.string.error_register_requires_internet))
            )
        }

        return@withContext try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val uid = authResult.user?.uid
                ?: throw Exception(appContext.getString(R.string.error_uid))

            val shelter = Shelter(uid, name, address, contact, email)
            val shelterData = shelter.toFirebaseMap().toMutableMap()
            shelterData["accountType"] = "SHELTER"

            firestore.collection("shelters")
                .document(uid)
                .set(shelterData)
                .await()

            Result.success(shelter)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -----------------------------------------------------------
    // LOGIN
    // -----------------------------------------------------------
    suspend fun login(email: String, password: String): Result<LoginResult> =
        withContext(Dispatchers.IO) {

            if (!NetworkUtils.isConnected()) {
                return@withContext Result.failure(
                    Exception(appContext.getString(R.string.error_login_requires_internet))
                )
            }

            return@withContext try {
                val authResult = firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .await()

                val uid = authResult.user?.uid
                    ?: throw Exception(appContext.getString(R.string.error_uid_not_found))

                // ------------------ USER ------------------
                val userDoc = firestore.collection("users").document(uid).get().await()
                if (userDoc.exists()) {
                    val user = userDoc.toUser()!!.copy(id = uid)
                    userDao.insert(user)
                    return@withContext Result.success(
                        LoginResult(user = user, accountType = AccountType.USER)
                    )
                }

                // ------------------ SHELTER ------------------
                val shelterDoc = firestore.collection("shelters").document(uid).get().await()
                if (shelterDoc.exists()) {
                    val shelter = shelterDoc.toShelter()!!.copy(id = uid)
                    shelterDao.insert(shelter)
                    return@withContext Result.success(
                        LoginResult(shelter = shelter, accountType = AccountType.SHELTER)
                    )
                }

                throw Exception(appContext.getString(R.string.error_account_not_found))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // -----------------------------------------------------------
    // CHECK SESSION
    // -----------------------------------------------------------
    suspend fun checkSession(): Result<LoginResult?> = withContext(Dispatchers.IO) {

        val uid = firebaseAuth.currentUser?.uid ?: return@withContext Result.success(null)

        if (!NetworkUtils.isConnected()) {

            val localUser = userDao.getUserById(uid)
            if (localUser != null) {
                return@withContext Result.success(
                    LoginResult(user = localUser, accountType = AccountType.USER)
                )
            }

            val localShelter = shelterDao.getShelterById(uid)
            if (localShelter != null) {
                return@withContext Result.success(
                    LoginResult(shelter = localShelter, accountType = AccountType.SHELTER)
                )
            }

            return@withContext Result.success(null)
        }

        return@withContext try {
            firebaseAuth.currentUser?.getIdToken(true)?.await()

            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toUser()!!.copy(id = uid)
                userDao.insert(user)
                return@withContext Result.success(
                    LoginResult(user = user, accountType = AccountType.USER)
                )
            }

            val shelterDoc = firestore.collection("shelters").document(uid).get().await()
            if (shelterDoc.exists()) {
                val shelter = shelterDoc.toShelter()!!.copy(id = uid)
                shelterDao.insert(shelter)
                return@withContext Result.success(
                    LoginResult(shelter = shelter, accountType = AccountType.SHELTER)
                )
            }

            Result.success(null)

        } catch (e: Exception) {
            firebaseAuth.signOut()
            Result.failure(e)
        }
    }

    /** Logs out the current Firebase account. */
    fun logout() = firebaseAuth.signOut()

    /** Returns the UID of the currently logged-in user. */
    fun getCurrentUserId() = firebaseAuth.currentUser?.uid
}
