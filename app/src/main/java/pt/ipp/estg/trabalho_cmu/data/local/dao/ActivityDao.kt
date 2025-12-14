package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity

/**
 * DAO responsible for managing Activity entities in the local Room database.
 *
 * Provides queries for:
 * - Retrieving activities by user
 * - Retrieving activities by animal
 * - Filtering active activities
 * - CRUD operations
 */
@Dao
interface ActivityDao {

    /**
     * Retrieves all upcoming activities for a specific user.
     * Upcoming means the delivery date is greater than or equal to the current date.
     *
     * @param userId The user ID to filter by.
     * @param currentDate Current date in dd/MM/yyyy format.
     * @return LiveData list of upcoming activities ordered by pickup date.
     */
    @Query("""
        SELECT * FROM activities
        WHERE userId = :userId
        AND deliveryDate >= :currentDate
        ORDER BY pickupDate ASC
    """)
    fun getUpcomingActivitiesByUser(userId: String, currentDate: String): LiveData<List<Activity>>

    /**
     * Retrieves all activities for a specific user, ordered by pickup date descending.
     *
     * @param userId The user ID to filter by.
     * @return LiveData list of all user activities.
     */
    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY pickupDate DESC")
    fun getAllActivitiesByUser(userId: String): LiveData<List<Activity>>

    /**
     * Retrieves a single activity by its ID.
     *
     * @param activityId The activity ID.
     * @return The activity or null if not found.
     */
    @Query("SELECT * FROM activities WHERE id = :activityId LIMIT 1")
    suspend fun getActivityById(activityId: String): Activity?

    /**
     * Retrieves all activities for a specific animal, ordered by pickup date.
     *
     * @param animalId The animal ID to filter by.
     * @return LiveData list of activities for the animal.
     */
    @Query("SELECT * FROM activities WHERE animalId = :animalId ORDER BY pickupDate ASC")
    fun getActivitiesByAnimal(animalId: String): LiveData<List<Activity>>

    /**
     * Retrieves active activities for a specific animal.
     * Active means the current date falls within the activity period
     * (pickupDate <= currentDate <= deliveryDate).
     *
     * @param animalId The animal ID to filter by.
     * @param currentDate Current date in dd/MM/yyyy format.
     * @return List of active activities for the animal.
     */
    @Query("""
        SELECT * FROM activities
        WHERE animalId = :animalId
        AND pickupDate  <= :currentDate
        AND deliveryDate >= :currentDate
        ORDER BY pickupDate ASC
    """)
    suspend fun getActiveActivitiesByAnimal(animalId: String, currentDate: String): List<Activity>

    /**
     * Inserts a single activity into the database.
     * If an activity with the same ID exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: Activity)

    /**
     * Inserts multiple activities into the database.
     * If activities with the same IDs exist, they will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<Activity>)

    /**
     * Updates an existing activity.
     */
    @Update
    suspend fun update(activity: Activity)

    /**
     * Deletes a specific activity.
     */
    @Delete
    suspend fun delete(activity: Activity)

    /**
     * Deletes all activities from the database.
     */
    @Query("DELETE FROM activities")
    suspend fun deleteAll()

    /**
     * Deletes all activities for a specific user.
     * Used before syncing activities from Firebase.
     *
     * @param userId The user ID whose activities should be deleted.
     */
    @Query("DELETE FROM activities WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    /**
     * Clears all activities and inserts new data.
     * Used during full synchronization with remote backend.
     */
    @Transaction
    suspend fun refreshCache(activities: List<Activity>) {
        deleteAll()
        insertAll(activities)
    }
}