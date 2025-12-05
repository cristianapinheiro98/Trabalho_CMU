package pt.ipp.estg.trabalho_cmu.data.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.*
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toActivity
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val application: Application
) {
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
    private val TAG = "ActivityRepository"

    fun getUpcomingActivitiesByUser(userId: String): LiveData<List<Activity>> {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        return activityDao.getUpcomingActivitiesByUser(userId, currentDate)
    }
    fun getAllActivitiesByUser(userId: String) = activityDao.getAllActivitiesByUser(userId)
    fun getActivitiesByAnimal(animalId: String) = activityDao.getActivitiesByAnimal(animalId)
    suspend fun getActivityById(id: String) = activityDao.getActivityById(id)

    suspend fun createActivity(activity: Activity): Result<Activity> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))
            val docRef = firestore.collection("activities").add(activity.toFirebaseMap()).await()
            val savedActivity = activity.copy(id = docRef.id)
            Result.success(savedActivity)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateActivity(activity: Activity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))
            firestore.collection("activities").document(activity.id).set(activity.toFirebaseMap()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteActivity(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))
            firestore.collection("activities").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun syncActivities(userId: String) = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext
        try {
            val snapshot = firestore.collection("activities").whereEqualTo("userId", userId).get().await()
            val activities = snapshot.documents.mapNotNull { it.toActivity() }
            activityDao.deleteAllByUser(userId)
            activityDao.insertAll(activities)
        } catch (e: Exception) { Log.e(TAG, "Sync Error", e) }
    }
}