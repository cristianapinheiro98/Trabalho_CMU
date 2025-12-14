package pt.ipp.estg.trabalho_cmu.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.providers.FirebaseProvider
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.*
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toActivity
import pt.ipp.estg.trabalho_cmu.data.models.mappers.toFirebaseMap

/**
 * Repository responsible for managing activity-related data.
 *
 * Handles:
 * - CRUD operations for activities
 * - Synchronizing Firebase data to Room cache
 * - Querying activities by user, animal, or date ranges
 */
class ActivityRepository(
    private val activityDao: ActivityDao,
    private val animalDao: AnimalDao
) {
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
    private val TAG = "ActivityRepository"

    /**
     * Gets upcoming activities for a specific user.
     * Upcoming activities have pickupDate >= currentDate.
     *
     * @param userId The ID of the user
     * @return LiveData list of upcoming activities
     */
    fun getUpcomingActivitiesByUser(userId: String): LiveData<List<Activity>> {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        return activityDao.getUpcomingActivitiesByUser(userId, currentDate)
    }

    /**
     * Gets all activities for a specific user.
     *
     * @param userId The ID of the user
     * @return LiveData list of all activities
     */
    fun getAllActivitiesByUser(userId: String) = activityDao.getAllActivitiesByUser(userId)

    /**
     * Gets all activities for a specific animal.
     *
     * @param animalId The ID of the animal
     * @return LiveData list of activities
     */
    fun getActivitiesByAnimal(animalId: String): LiveData<List<Activity>> {
        return activityDao.getActivitiesByAnimal(animalId)
    }

    /**
     * Retrieves a single activity by its ID from the local database.
     *
     * @param id The activity ID
     * @return The activity if found, null otherwise
     */
    suspend fun getActivityById(id: String) = activityDao.getActivityById(id)

    /**
     * Creates a new activity in Firebase.
     *
     * @param activity The activity to create
     * @return Result containing the saved activity with Firebase-generated ID, or an error
     */
    suspend fun createActivity(activity: Activity): Result<Activity> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))
            val docRef = firestore.collection("activities").add(activity.toFirebaseMap()).await()
            val savedActivity = activity.copy(id = docRef.id)
            Result.success(savedActivity)
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Updates an existing activity in Firebase.
     *
     * @param activity The activity with updated data
     * @return Result indicating success or failure
     */
    suspend fun updateActivity(activity: Activity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))
            firestore.collection("activities").document(activity.id).set(activity.toFirebaseMap()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Deletes an activity from Firebase.
     *
     * @param id The ID of the activity to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteActivity(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!NetworkUtils.isConnected()) return@withContext Result.failure(Exception("Offline"))
            firestore.collection("activities").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Syncs all activities for a specific user from Firebase to Room.
     *
     * This method fetches all activities where userId matches, deletes existing cached activities
     * for that user, and inserts the fresh data from Firebase.
     *
     * @param userId The ID of the user whose activities should be synced
     */
    suspend fun syncActivities(userId: String) = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext
        try {
            val snapshot = firestore.collection("activities")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val activities = snapshot.documents.mapNotNull { it.toActivity() }

            val validActivities = activities.filter { activity ->
                animalDao.getAnimalById(activity.animalId) != null
            }

            activityDao.deleteAllByUser(userId)
            if (validActivities.isNotEmpty()) {
                activityDao.insertAll(validActivities)
            }

            Log.d(TAG, "SyncActivities: ${validActivities.size}/${activities.size} activities synced for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Sync Error", e)
        }
    }

    /**
     * Fetches booked dates for an animal directly from Firebase.
     * Does not insert into Room to avoid foreign key issues with other users' activities.
     *
     * @param animalId The ID of the animal
     * @return List of booked date strings in "dd/MM/yyyy" format
     */
    suspend fun getBookedDatesFromFirebase(animalId: String): List<String> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isConnected()) return@withContext emptyList()

        try {
            val snapshot = firestore.collection("activities")
                .whereEqualTo("animalId", animalId)
                .get()
                .await()

            val activities = snapshot.documents.mapNotNull { it.toActivity() }
            val bookedDates = mutableListOf<String>()
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()

            activities.forEach { activity ->
                val start = sdf.parse(activity.pickupDate) ?: return@forEach
                val end = sdf.parse(activity.deliveryDate) ?: return@forEach

                calendar.time = start
                while (!calendar.time.after(end)) {
                    bookedDates.add(sdf.format(calendar.time))
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            Log.d(TAG, "GetBookedDatesFromFirebase: ${bookedDates.size} dates for animal $animalId")
            bookedDates
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching booked dates for animal $animalId", e)
            emptyList()
        }
    }

    /**
     * Gets all active activities for a specific animal.
     * Active activities are those with deliveryDate >= currentDate.
     *
     * @param animalId The ID of the animal
     * @param currentDate Current date in "dd/MM/yyyy" format
     * @return List of active activities for the animal
     */
    suspend fun getActiveActivitiesByAnimal(animalId: String, currentDate: String): List<Activity> {
        return activityDao.getActiveActivitiesByAnimal(animalId, currentDate)
    }
}