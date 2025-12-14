package pt.ipp.estg.trabalho_cmu.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pt.ipp.estg.trabalho_cmu.MainActivity
import pt.ipp.estg.trabalho_cmu.R

/**
 * Manages system notifications for the TailWagger app.
 *
 * Features:
 * - New animal added notifications
 * - Ownership request accepted/rejected notifications
 * - Notification channels for Android 8.0+
 * - Deep linking to specific screens
 * - Notification permission handling for Android 13+
 */
object NotificationManager {
    // Notification Channel IDs
    private const val CHANNEL_ANIMALS = "animals_channel"
    private const val CHANNEL_OWNERSHIP = "ownership_channel"

    // Notification IDs (unique for each type)
    private const val NOTIFICATION_ID_NEW_ANIMAL = 1001
    private const val NOTIFICATION_ID_OWNERSHIP_ACCEPTED = 2001
    private const val NOTIFICATION_ID_OWNERSHIP_REJECTED = 2002

    /**
     * Creates notification channels for Android 8.0+ (API 26+).
     *
     * Call this method once when the app starts (e.g., in Application.onCreate()
     * or MainActivity.onCreate()).
     *
     * Channels:
     * - Animals: For new animal additions
     * - Ownership: For ownership request updates
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel 1: New Animals
            val animalsChannel = NotificationChannel(
                CHANNEL_ANIMALS,
                context.getString(R.string.notification_channel_animals),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_animals_description)
                enableVibration(true)
                enableLights(true)
            }

            // Channel 2: Ownership Updates
            val ownershipChannel = NotificationChannel(
                CHANNEL_OWNERSHIP,
                context.getString(R.string.notification_channel_ownership),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_ownership_description)
                enableVibration(true)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(animalsChannel)
            notificationManager.createNotificationChannel(ownershipChannel)
        }
    }

    /**
     * Check if the app has notification permission (Android 13+).
     *
     * @return true if permission granted or not required (Android < 13)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for Android < 13
        }
    }

    /**
     * Notify user about a new animal added to the shelter.
     *
     * @param context Application context
     * @param animalName Name of the new animal
     * @param animalId ID of the animal (for deep linking)
     */
    fun notifyNewAnimal(context: Context, animalName: String, animalId: String) {
        if (!hasNotificationPermission(context)) {
            return
        }

        // Create intent to open animal detail screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("animal_id", animalId)
            putExtra("screen", "animal_detail")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ANIMALS)
            .setSmallIcon(R.drawable.ic_collar)
            .setContentTitle(context.getString(R.string.notification_new_animal_title))
            .setContentText(context.getString(R.string.notification_new_animal_message, animalName))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_new_animal_big_text, animalName))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_NEW_ANIMAL + animalId.hashCode(), notification)
        }
    }

    /**
     * Notify user that their ownership request was accepted.
     *
     * @param context Application context
     * @param animalName Name of the animal
     * @param animalId ID of the animal (for deep linking)
     */
    fun notifyOwnershipAccepted(context: Context, animalName: String, animalId: String) {
        if (!hasNotificationPermission(context)) {
            return
        }

        // Create intent to open ownership details screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("animal_id", animalId)
            putExtra("screen", "ownership_detail")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OWNERSHIP)
            .setSmallIcon(R.drawable.ic_collar)
            .setContentTitle(context.getString(R.string.notification_ownership_accepted_title))
            .setContentText(context.getString(R.string.notification_ownership_accepted_message, animalName))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_ownership_accepted_big_text, animalName))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_OWNERSHIP_ACCEPTED + animalId.hashCode(), notification)
        }
    }

    /**
     * Notify user that their ownership request was rejected.
     *
     * @param context Application context
     * @param animalName Name of the animal
     * @param reason Optional reason for rejection
     */
    fun notifyOwnershipRejected(context: Context, animalName: String) {
        if (!hasNotificationPermission(context)) {
            return
        }

        // Create intent to open main screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("screen", "ownership_list")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OWNERSHIP)
            .setSmallIcon(R.drawable.ic_collar)
            .setContentTitle(context.getString(R.string.notification_ownership_rejected_title))
            .setContentText(context.getString(R.string.notification_ownership_rejected_message, animalName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_OWNERSHIP_REJECTED + animalName.hashCode(), notification)
        }
    }

    /**
     * Cancel all notifications from the app.
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Cancel a specific notification by ID.
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}