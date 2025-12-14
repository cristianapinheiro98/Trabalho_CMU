package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk

/**
 * Data Access Object for Walk entities.
 *
 * Provides methods to query, insert, and delete walks from Room database.
 * Includes queries for both personal walk history and public SocialTails
 * community feed with support for pagination.
 */
@Dao
interface WalkDao {

    /**
     * Get all walks for a specific user, ordered by most recent.
     *
     * @param userId User ID to fetch walks for
     * @return LiveData list of walks
     */
    @Query("SELECT * FROM walks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWalksByUser(userId: String): LiveData<List<Walk>>

    /**
     * Get walks for a user with pagination support.
     *
     * @param userId User ID to fetch walks for
     * @param limit Number of walks to fetch
     * @param offset Number of walks to skip
     * @return List of walks
     */
    @Query("SELECT * FROM walks WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getWalksByUserPaginated(userId: String, limit: Int, offset: Int): List<Walk>

    /**
     * Get a specific walk by ID.
     *
     * @param walkId Walk ID to fetch
     * @return Walk or null if not found
     */
    @Query("SELECT * FROM walks WHERE id = :walkId")
    suspend fun getWalkById(walkId: String): Walk?

    /**
     * Get the most recent walk for a user.
     *
     * @param userId User ID to fetch last walk for
     * @return Walk or null if no walks exist
     */
    @Query("SELECT * FROM walks WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastWalk(userId: String): Walk?

    /**
     * Get recent walks that earned medals.
     *
     * @param userId User ID to fetch medals for
     * @param limit Number of medal walks to fetch
     * @return List of walks with medals
     */
    @Query("SELECT * FROM walks WHERE userId = :userId AND medalType IS NOT NULL ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentMedals(userId: String, limit: Int): List<Walk>

    /**
     * Get all public walks ordered by most recent.
     * Used for the SocialTails community feed.
     *
     * @return LiveData list of public walks
     */
    @Query("SELECT * FROM walks WHERE isPublic = 1 ORDER BY createdAt DESC")
    fun getPublicWalks(): LiveData<List<Walk>>

    /**
     * Get public walks with pagination support.
     * Used for the SocialTails community feed with infinite scroll.
     *
     * @param limit Number of walks to fetch
     * @param offset Number of walks to skip
     * @return List of public walks
     */
    @Query("SELECT * FROM walks WHERE isPublic = 1 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPublicWalksPaginated(limit: Int, offset: Int): List<Walk>

    /**
     * Get top walks of all time by duration.
     * Used for the all-time podium in SocialTails community.
     *
     * @param limit Number of top walks to fetch (default 3 for podium)
     * @return List of top walks ordered by duration descending
     */
    @Query("SELECT * FROM walks WHERE isPublic = 1 ORDER BY duration DESC LIMIT :limit")
    suspend fun getTopWalksAllTime(limit: Int = 3): List<Walk>

    /**
     * Get top walks for current month by duration.
     * Used for the monthly podium in SocialTails community.
     *
     * @param monthStart Start timestamp of the current month
     * @param monthEnd End timestamp of the current month
     * @param limit Number of top walks to fetch (default 3 for podium)
     * @return List of top walks for the month ordered by duration descending
     */
    @Query("SELECT * FROM walks WHERE isPublic = 1 AND createdAt >= :monthStart AND createdAt < :monthEnd ORDER BY duration DESC LIMIT :limit")
    suspend fun getTopWalksMonthly(monthStart: Long, monthEnd: Long, limit: Int = 3): List<Walk>

    /**
     * Update a walk's public status.
     * Used when user shares a walk to SocialTails community.
     *
     * @param walkId Walk ID to update
     * @param isPublic New public status
     */
    @Query("UPDATE walks SET isPublic = :isPublic WHERE id = :walkId")
    suspend fun updateWalkPublicStatus(walkId: String, isPublic: Boolean)

    /**
     * Insert or replace a walk.
     *
     * @param walk Walk to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalk(walk: Walk)

    /**
     * Insert or replace multiple walks.
     *
     * @param walks List of walks to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalks(walks: List<Walk>)

    /**
     * Delete a walk.
     *
     * @param walk Walk to delete
     */
    @Delete
    suspend fun deleteWalk(walk: Walk)

    /**
     * Delete a walk by ID.
     *
     * @param walkId Walk ID to delete
     */
    @Query("DELETE FROM walks WHERE id = :walkId")
    suspend fun deleteWalkById(walkId: String)

    /**
     * Delete all walks for a user.
     *
     * @param userId User ID to delete walks for
     */
    @Query("DELETE FROM walks WHERE userId = :userId")
    suspend fun deleteWalksByUser(userId: String)
}