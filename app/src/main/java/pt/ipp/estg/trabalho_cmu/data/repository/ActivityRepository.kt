package pt.ipp.estg.trabalho_cmu.data.repository

import androidx.lifecycle.LiveData
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import java.text.SimpleDateFormat
import java.util.*

class ActivityRepository(private val activityDao: ActivityDao) {


    fun getUpcomingActivitiesByUser(userId: Int): LiveData<List<Activity>> {
        val currentDate = getCurrentDateString()
        return activityDao.getUpcomingActivitiesByUser(userId, currentDate)
    }

    suspend fun getActivityById(activityId: Int): Activity? =
        activityDao.getActivityById(activityId)

    suspend fun addActivity(activity: Activity) {
        activityDao.insertActivity(activity)
    }

    suspend fun updateActivity(activity: Activity) {
        activityDao.updateActivity(activity)
    }


    suspend fun deleteActivity(activity: Activity) {
        activityDao.deleteActivity(activity)
    }

    suspend fun deleteAllActivitiesByUser(userId: Int) {
        activityDao.deleteAllActivitiesByUser(userId)
    }

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}