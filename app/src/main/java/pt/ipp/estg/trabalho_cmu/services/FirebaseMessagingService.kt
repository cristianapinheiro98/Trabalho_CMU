package pt.ipp.estg.trabalho_cmu.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for receiving push notifications in TailWagger.
 *
 * This service handles:
 * - Receiving push notifications when app is in background/foreground
 * - Updating FCM token when it changes
 * - Processing notification payload and triggering local notifications
 *
 * Notification Types (based on "type" field in payload):
 * - "new_animal": New animal added to shelter
 * - "ownership_accepted": Ownership request accepted
 * - "ownership_rejected": Ownership request rejected
 *
 */
class FirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "TailWaggerFCM"

        // Notification payload keys
        private const val KEY_TYPE = "type"
        private const val KEY_ANIMAL_ID = "animal_id"
        private const val KEY_ANIMAL_NAME = "animal_name"
        private const val KEY_REASON = "reason"

        // Notification types
        private const val TYPE_NEW_ANIMAL = "new_animal"
        private const val TYPE_OWNERSHIP_ACCEPTED = "ownership_accepted"
        private const val TYPE_OWNERSHIP_REJECTED = "ownership_rejected"
    }

    /**
     * Called when a new FCM token is generated.
     * This happens on initial app install, after clearing app data,
     * or when the token is rotated.
     *
     * Save this token to your backend to send notifications to this device.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // TODO: Send token to your backend server
        // Example:
        // UserRepository.updateFcmToken(token)

        // Save token locally for reference
        saveFcmToken(token)
    }

    /**
     * Called when a message is received.
     *
     * This is triggered when:
     * - App is in foreground
     * - App is in background and notification contains only data payload
     *
     * If notification contains both notification and data payload and app is
     * in background, the notification tray handles the notification and this
     * method receives the data payload.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Check if message contains data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataPayload(message.data)
        }

        // Check if message contains notification payload
        message.notification?.let {
            Log.d(TAG, "Message notification: ${it.title} - ${it.body}")
            // If app is in foreground, notification payload is ignored by default
            // We handle it manually with local notifications
        }
    }

    /**
     * Process the data payload and trigger appropriate local notification.
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val type = data[KEY_TYPE] ?: return

        when (type) {
            TYPE_NEW_ANIMAL -> {
                val animalName = data[KEY_ANIMAL_NAME] ?: return
                val animalId = data[KEY_ANIMAL_ID] ?: return

                Log.i(TAG, "New animal notification: $animalName (ID: $animalId)")
                NotificationManager.notifyNewAnimal(
                    context = applicationContext,
                    animalName = animalName,
                    animalId = animalId
                )
            }

            TYPE_OWNERSHIP_ACCEPTED -> {
                val animalName = data[KEY_ANIMAL_NAME] ?: return
                val animalId = data[KEY_ANIMAL_ID] ?: return

                Log.i(TAG, "Ownership accepted notification: $animalName (ID: $animalId)")
                NotificationManager.notifyOwnershipAccepted(
                    context = applicationContext,
                    animalName = animalName,
                    animalId = animalId
                )
            }

            TYPE_OWNERSHIP_REJECTED -> {
                val animalName = data[KEY_ANIMAL_NAME] ?: return
                val reason = data[KEY_REASON]

                Log.i(TAG, "Ownership rejected notification: $animalName")
                NotificationManager.notifyOwnershipRejected(
                    context = applicationContext,
                    animalName = animalName
                )
            }

            else -> {
                Log.w(TAG, "Unknown notification type: $type")
            }
        }
    }

    /**
     * Save FCM token to SharedPreferences.
     */
    private fun saveFcmToken(token: String) {
        val prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("fcm_token", token)
            .apply()

        Log.d(TAG, "FCM token saved locally")
    }

    /**
     * Get saved FCM token from SharedPreferences.
     */
    fun getSavedFcmToken(): String? {
        val prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
        return prefs.getString("fcm_token", null)
    }
}