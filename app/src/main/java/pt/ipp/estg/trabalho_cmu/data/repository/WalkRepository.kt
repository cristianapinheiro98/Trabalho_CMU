package pt.ipp.estg.trabalho_cmu.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.trabalho_cmu.data.local.dao.WalkDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk
import pt.ipp.estg.trabalho_cmu.data.models.mappers.WalkMapper.toFirebaseMap
import pt.ipp.estg.trabalho_cmu.data.models.mappers.WalkMapper.toWalk
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import java.util.Calendar

/**
 * Repository for managing Walk data.
 *
 * Implements online-first strategy with Room backup for offline access.
 * Provides methods for both personal walk history and public SocialTails
 * community features including podium rankings and feed.
 */
class WalkRepository(
    private val walkDao: WalkDao,
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
) {

    companion object {
        private const val TAG = "WalkRepository"
        private const val COLLECTION_WALKS = "walks"
    }

    /**
     * Get all walks for a user as LiveData.
     *
     * @param userId User ID to fetch walks for
     * @return LiveData list of walks from Room
     */
    fun getWalksByUser(userId: String): LiveData<List<Walk>> {
        return walkDao.getWalksByUser(userId)
    }

    /**
     * Get paginated walks for history screen.
     *
     * @param userId User ID to fetch walks for
     * @param limit Number of walks per page
     * @param offset Number of walks to skip
     * @return List of walks from Room
     */
    suspend fun getWalksByUserPaginated(userId: String, limit: Int, offset: Int): List<Walk> {
        return walkDao.getWalksByUserPaginated(userId, limit, offset)
    }

    /**
     * Get a specific walk by ID.
     *
     * @param walkId Walk ID to fetch
     * @return Walk or null if not found
     */
    suspend fun getWalkById(walkId: String): Walk? {
        return walkDao.getWalkById(walkId)
    }

    /**
     * Get the most recent walk for main screen display.
     *
     * @param userId User ID to fetch last walk for
     * @return Walk or null if no walks exist
     */
    suspend fun getLastWalk(userId: String): Walk? {
        return walkDao.getLastWalk(userId)
    }

    /**
     * Get recent walks that earned medals.
     *
     * @param userId User ID to fetch medals for
     * @param limit Number of recent medals to fetch (default 5)
     * @return List of walks with medals
     */
    suspend fun getRecentMedals(userId: String, limit: Int = 5): List<Walk> {
        return walkDao.getRecentMedals(userId, limit)
    }

    /**
     * Get public walks feed from Firebase with pagination.
     * Fetches directly from Firebase to get walks from all users.
     *
     * @param limit Number of walks per page
     * @param offset Number of walks to skip (for pagination)
     * @return Result with list of public walks or error
     */
    suspend fun getPublicWalksPaginated(limit: Int, offset: Int): Result<List<Walk>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_WALKS)
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit((limit + offset).toLong())
                .get()
                .await()

            val allWalks = snapshot.documents.mapNotNull { it.toWalk() }
            val paginatedWalks = allWalks.drop(offset).take(limit)

            Log.d(TAG, "Fetched ${paginatedWalks.size} public walks from Firebase")
            Result.success(paginatedWalks)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching public walks", e)
            Result.failure(e)
        }
    }

    /**
     * Get top walks of all time by duration from Firebase.
     * Used for the all-time podium in SocialTails community.
     *
     * @param limit Number of top walks to fetch (default 3 for podium)
     * @return Result with list of top walks or error
     */
    suspend fun getTopWalksAllTime(limit: Int = 3): Result<List<Walk>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_WALKS)
                .whereEqualTo("isPublic", true)
                .orderBy("duration", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val walks = snapshot.documents.mapNotNull { it.toWalk() }
            Log.d(TAG, "Fetched ${walks.size} top all-time walks from Firebase")
            Result.success(walks)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching top all-time walks", e)
            Result.failure(e)
        }
    }

    /**
     * Get top walks for current month by duration from Firebase.
     * Used for the monthly podium in SocialTails community.
     *
     * @param limit Number of top walks to fetch (default 3 for podium)
     * @return Result with list of top walks for the month or error
     */
    suspend fun getTopWalksMonthly(limit: Int = 3): Result<List<Walk>> {
        return try {
            // Calculate start and end of current month
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.timeInMillis

            calendar.add(Calendar.MONTH, 1)
            val monthEnd = calendar.timeInMillis

            val snapshot = firestore.collection(COLLECTION_WALKS)
                .whereEqualTo("isPublic", true)
                .whereGreaterThanOrEqualTo("createdAt", monthStart)
                .whereLessThan("createdAt", monthEnd)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            // Sort by duration locally since Firebase doesn't support
            // orderBy on a different field after whereGreaterThan
            val walks = snapshot.documents
                .mapNotNull { it.toWalk() }
                .sortedByDescending { it.duration }
                .take(limit)

            Result.success(walks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share a walk to SocialTails community.
     * Updates the walk's isPublic flag in both Firebase and Room.
     *
     * @param walkId Walk ID to share
     * @return Result with Unit or error
     */
    suspend fun shareWalkToSocialTails(walkId: String): Result<Unit> {
        return try {
            // Update in Firebase
            firestore.collection(COLLECTION_WALKS)
                .document(walkId)
                .update("isPublic", true)
                .await()

            // Update in Room
            walkDao.updateWalkPublicStatus(walkId, true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a new walk in Firebase and backup to Room.
     * Online-first approach: Firebase is source of truth.
     *
     * @param walk Walk to create
     * @return Result with created Walk or error
     */
    suspend fun createWalk(walk: Walk): Result<Walk> {
        return try {
            // Create in Firebase first
            val docRef = firestore.collection(COLLECTION_WALKS).document()
            val walkWithId = walk.copy(id = docRef.id)

            docRef.set(walkWithId.toFirebaseMap()).await()

            // Backup to Room
            walkDao.insertWalk(walkWithId)

            Result.success(walkWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a walk from Firebase and Room.
     *
     * @param walkId Walk ID to delete
     * @return Result with Unit or error
     */
    suspend fun deleteWalk(walkId: String): Result<Unit> {
        return try {
            // Delete from Firebase
            firestore.collection(COLLECTION_WALKS).document(walkId).delete().await()

            // Delete from Room
            walkDao.deleteWalkById(walkId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync walks from Firebase to Room for offline access.
     *
     * @param userId User ID to sync walks for
     * @return Result with Unit or error
     */
    suspend fun syncWalks(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION_WALKS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val walks = snapshot.documents.mapNotNull { it.toWalk() }

            var successCount = 0
            walks.forEach { walk ->
                try {
                    walkDao.insertWalk(walk)
                    successCount++
                } catch (e: Exception) {
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync public walks from Firebase to Room for offline caching.
     * Only syncs a limited number for performance.
     *
     * @param limit Number of public walks to sync
     * @return Result with Unit or error
     */
    suspend fun syncPublicWalks(limit: Int = 20): Result<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION_WALKS)
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val walks = snapshot.documents.mapNotNull { it.toWalk() }

            var successCount = 0
            walks.forEach { walk ->
                try {
                    walkDao.insertWalk(walk)
                    successCount++
                } catch (e: Exception) {
                    Log.w(TAG, "Skipping public walk ${walk.id} due to constraint")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing public walks", e)
            Result.failure(e)
        }
    }
}