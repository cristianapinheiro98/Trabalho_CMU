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

    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY pickupDate DESC")
    fun getAllActivitiesByUser(userId: String): LiveData<List<Activity>>

    @Query("SELECT * FROM activities WHERE id = :activityId LIMIT 1")
    suspend fun getActivityById(activityId: String): Activity?

    @Query("SELECT * FROM activities WHERE animalId = :animalId ORDER BY pickupDate ASC")
    fun getActivitiesByAnimal(animalId: String): LiveData<List<Activity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: Activity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<Activity>)

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)

    @Query("DELETE FROM activities")
    suspend fun deleteAll()

    // --- MÉTODOS EM FALTA ADICIONADOS ---

    @Query("DELETE FROM activities WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Transaction
    suspend fun refreshCache(activities: List<Activity>) {
        deleteAll()
        insertAll(activities)
    }
}