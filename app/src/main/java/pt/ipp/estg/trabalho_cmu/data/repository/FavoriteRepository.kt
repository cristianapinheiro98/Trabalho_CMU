package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.local.dao.FavoriteDao
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFavorite
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils

/**
 * Repository responsible for managing Favorite data across
 * local persistence (Room) and remote backend (Firebase Firestore).
 *
 * Provides an offline-first approach:
 * - Writes temporary favorites locally before remote sync
 * - Synchronizes Firestore favorites into the local database
 *
 * @property appContext Application context used for string resources.
 * @property favoriteDao DAO used to access the local favorites table.
 */
class FavoriteRepository(
    private val appContext: Context,
    private val favoriteDao: FavoriteDao
) {

    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
    private val TAG = "FavoriteRepository"

    /**
     * Returns a LiveData list of favorites for the given user.
     *
     * This method observes local Room data, which can be kept in sync
     * with Firestore using [syncFavorites].
     *
     * @param userId ID of the user whose favorites will be observed.
     * @return LiveData emitting the list of Favorite entities.
     */
    fun getFavoritesByUserLive(userId: String): LiveData<List<Favorite>> =
        favoriteDao.getFavoritesByUserLive(userId)

    /**
     * Returns the list of favorites for the given user as a suspend function.
     *
     * This is a one-shot query, not observable, reading directly from Room.
     *
     * @param userId ID of the user whose favorites will be loaded.
     * @return List of Favorite entities for that user.
     */
    suspend fun getFavoritesByUser(userId: String): List<Favorite> =
        favoriteDao.getFavoritesByUser(userId)

    /**
     * Checks whether a specific animal is marked as favorite for a user.
     *
     * @param userId ID of the user.
     * @param animalId ID of the animal.
     * @return true if a matching Favorite exists, false otherwise.
     */
    suspend fun isFavorite(userId: String, animalId: String): Boolean =
        favoriteDao.getFavorite(userId, animalId) != null


    /**
     * Adds a favorite for the given user, synchronizing Room and Firestore.
     *
     * Flow:
     * 1. Inserts a temporary Favorite locally with a generated temp ID.
     * 2. If offline, returns a failure Result and keeps the temp record.
     * 3. If online, creates the document in Firestore.
     * 4. Inserts the Favorite again locally with the Firestore document ID.
     *
     * @param userId ID of the user adding the favorite (used for filtering).
     * @param favorite Favorite entity to be added (ID will be replaced by Firestore ID).
     * @return Result containing the saved Favorite with a valid Firestore ID on success.
     */
    suspend fun addFavorite(userId: String, favorite: Favorite): Result<Favorite> =
        withContext(Dispatchers.IO) {
            try {
                val tempFavorite = favorite.copy(id = "temp_${System.currentTimeMillis()}")
                favoriteDao.insertFavorite(tempFavorite)

                if (!NetworkUtils.isConnected()) {
                    val msg = appContext.getString(R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                val docRef = firestore.collection("favorites")
                    .add(favorite.toFirebaseMap()).await()

                val savedFavorite = favorite.copy(id = docRef.id)


                favoriteDao.insertFavorite(savedFavorite)


                Result.success(savedFavorite)

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adicionar favorito", e)
                Result.failure(e)
            }
        }

    /**
     * Removes a favorite relation between a user and an animal,
     * both locally and in Firestore.
     *
     * Flow:
     * 1. Remove the favorite from the local Room database.
     * 2. If offline, return a failure Result (local removal already happened).
     * 3. If online, query Firestore for matching documents and delete them.
     * 4. Ensure local data is clean by removing the favorite again by user/animal.
     *
     * @param userId ID of the user who owns the favorite.
     * @param animalId ID of the animal to be unfavorited.
     * @return Result<Unit> indicating success or failure.
     */
    suspend fun removeFavorite(userId: String, animalId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                favoriteDao.removeFavorite(userId, animalId)

                if (!NetworkUtils.isConnected()) {
                    val msg = appContext.getString(R.string.error_offline)
                    return@withContext Result.failure(Exception(msg))
                }

                val snapshot = firestore.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("animalId", animalId)
                    .get().await()

                snapshot.documents.forEach { it.reference.delete() }

                // Remover local
                favoriteDao.removeFavorite(userId, animalId)

                Result.success(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover favorito", e)
                Result.failure(e)
            }
        }


    /**
     * Synchronizes favorites for the given user from Firestore into Room.
     *
     * This method:
     * - Skips sync when there is no internet connection.
     * - Fetches all favorites from Firestore for the user.
     * - Converts documents to Favorite entities.
     * - Replaces the local list of favorites using a transactional DAO operation.
     *
     * @param userId ID of the user whose favorites will be synchronized.
     */
    suspend fun syncFavorites(userId: String) =
        withContext(Dispatchers.IO) {
            if (!NetworkUtils.isConnected()) return@withContext

            try {
                val snapshot = firestore.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .get().await()

                val favorites = snapshot.documents.mapNotNull { it.toFavorite() }

                favoriteDao.refreshFavoritesForUser(userId, favorites)

                Log.d(TAG, "SyncFavorites: ${favorites.size} favoritos sincronizados")

            } catch (e: Exception) {
                Log.e(TAG, "Erro SyncFavorites", e)
            }
        }
}
