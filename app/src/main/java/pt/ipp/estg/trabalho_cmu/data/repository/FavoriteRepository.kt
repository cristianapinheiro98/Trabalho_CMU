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


class FavoriteRepository(
    private val appContext: Context,
    private val favoriteDao: FavoriteDao
) {

    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
    private val TAG = "FavoriteRepository"


    fun getFavoritesByUserLive(userId: String): LiveData<List<Favorite>> =
        favoriteDao.getFavoritesByUserLive(userId)

    suspend fun getFavoritesByUser(userId: String): List<Favorite> =
        favoriteDao.getFavoritesByUser(userId)

    suspend fun isFavorite(userId: String, animalId: String): Boolean =
        favoriteDao.getFavorite(userId, animalId) != null



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
