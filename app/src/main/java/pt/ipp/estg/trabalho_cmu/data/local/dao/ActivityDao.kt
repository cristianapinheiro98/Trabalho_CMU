package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity

/**
 * Data Access Object (DAO) for the [Activity] entity.
 * This interface provides the methods that the rest of the app uses to interact
 * with the `activities` table in the database.
 */
@Dao
interface ActivityDao {

    /**
     * Retrieves all upcoming activities for a specific user, ordered by the pickup date.
     * An activity is considered "upcoming" if its delivery date is on or after the provided current date.
     *
     * @param userId The ID of the user whose activities are to be retrieved.
     * @param currentDate The current date in string format (e.g., "YYYY-MM-DD") to filter out past activities.
     * @return A [LiveData] list of [Activity] objects that are scheduled for the future,
     *         automatically updated when the data changes.
     */
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND deliveryDate >= :currentDate 
        ORDER BY pickupDate ASC
    """)
    fun getUpcomingActivitiesByUser(userId: Int, currentDate: String): LiveData<List<Activity>>

    /**
     * Fetches a single activity from the database by its unique ID.
     * This is a suspend function and must be called from a coroutine or another suspend function.
     *
     * @param activityId The ID of the activity to retrieve.
     * @return The [Activity] object if found, otherwise `null`.
     */
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Int): Activity?

    /**
     * Inserts a new activity into the `activities` table.
     * If an activity with the same primary key already exists, it will be replaced.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param activity The [Activity] object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    /**
     * Updates an existing activity in the database.
     * The activity is identified by its primary key.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param activity The [Activity] object with updated information.
     */
    @Update
    suspend fun updateActivity(activity: Activity)

    /**
     * Deletes a specific activity from the database.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param activity The [Activity] object to be deleted.
     */
    @Delete
    suspend fun deleteActivity(activity: Activity)

    /**
     * Deletes all activities associated with a specific user.
     * This is useful for clearing user-related data.
     * This is a suspend function and must be called from a coroutine.
     *
     * @param userId The ID of the user whose activities will be deleted.
     */
    @Query("DELETE FROM activities WHERE userId = :userId")
    suspend fun deleteAllActivitiesByUser(userId: Int)


}