package pt.ipp.estg.trabalho_cmu.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity

@Dao
interface ActivityDao {


    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND deliveryDate >= :currentDate 
        ORDER BY pickupDate ASC
    """)
    fun getUpcomingActivitiesByUser(userId: String, currentDate: String): LiveData<List<Activity>>

    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Int): Activity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Update
    suspend fun updateActivity(activity: Activity)

    @Delete
    suspend fun deleteActivity(activity: Activity)

    @Query("DELETE FROM activities WHERE userId = :userId")
    suspend fun deleteAllActivitiesByUser(userId: String)


}