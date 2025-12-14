package pt.ipp.estg.trabalho_cmu.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import pt.ipp.estg.trabalho_cmu.MainActivity
import pt.ipp.estg.trabalho_cmu.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Foreground service for tracking walk location in background
 * Provides GPS tracking with 1-second updates even when app is minimized
 */
class WalkTrackingService : Service() {

    companion object {
        private const val TAG = "WalkTrackingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "walk_tracking_channel"
        private const val LOCATION_UPDATE_INTERVAL = 1000L // 1 second
        private const val LOCATION_FASTEST_INTERVAL = 500L // 0.5 second
    }

    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager

    // Walk tracking state
    private var isTracking = false
    private var startTime: Long = 0
    private var routePoints = mutableListOf<LatLng>()
    private var totalDistance = 0.0 // in meters
    private var animalName: String = ""
    private var animalId: String = ""

    // Listeners for real-time updates
    private val listeners = mutableListOf<WalkUpdateListener>()

    /**
     * Binder for local service connection
     */
    inner class LocalBinder : Binder() {
        fun getService(): WalkTrackingService = this@WalkTrackingService
    }

    /**
     * Interface for receiving walk tracking updates
     */
    interface WalkUpdateListener {
        /**
         * Called when new location is received
         * @param location New GPS coordinates
         * @param distance Total distance in meters
         * @param duration Total duration in seconds
         */
        fun onLocationUpdate(location: LatLng, distance: Double, duration: Long)

        /**
         * Called when tracking state changes
         * @param isTracking True if tracking is active
         */
        fun onTrackingStateChanged(isTracking: Boolean)
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.walk_tracking_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.walk_tracking_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Setup callback for location updates
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "onLocationResult called with ${locationResult.locations.size} locations")
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "New location: lat=${location.latitude}, lng=${location.longitude}")
                    onNewLocation(location)
                } ?: Log.w(TAG, "lastLocation is null")
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                Log.d(TAG, "Location availability: ${availability.isLocationAvailable}")
            }
        }
    }

    /**
     * Start tracking a walk
     * @param animalId ID of the animal being walked
     * @param animalName Name of the animal for display
     */
    fun startTracking(animalId: String, animalName: String) {
        Log.d(TAG, "startTracking called with animalId=$animalId, animalName=$animalName")

        if (isTracking) {
            Log.w(TAG, "Already tracking")
            return
        }

        this.animalId = animalId
        this.animalName = animalName
        this.startTime = System.currentTimeMillis()
        this.routePoints.clear()
        this.totalDistance = 0.0
        this.isTracking = true

        Log.d(TAG, "Starting foreground service...")
        startForeground(NOTIFICATION_ID, createNotification())

        Log.d(TAG, "Starting location updates...")
        startLocationUpdates()

        notifyTrackingStateChanged()

        Log.d(TAG, "Started tracking walk with $animalName")
    }

    /**
     * Stop tracking and return walk data
     * @return WalkData containing all tracked information
     */
    fun stopTracking(): WalkData {
        if (!isTracking) {
            Log.w(TAG, "Not tracking")
            return WalkData(animalId, animalName, 0, 0.0, emptyList())
        }

        stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)

        val duration = (System.currentTimeMillis() - startTime) / 1000 // seconds
        val walkData = WalkData(
            animalId = animalId,
            animalName = animalName,
            duration = duration,
            distance = totalDistance,
            routePoints = routePoints.toList()
        )

        isTracking = false
        notifyTrackingStateChanged()

        Log.d(TAG, "Stopped tracking. Duration: ${duration}s, Distance: ${totalDistance}m")
        return walkData
    }

    /**
     * Request location updates from FusedLocationProvider
     */
    private fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called")

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            setWaitForAccurateLocation(true)
        }.build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Missing ACCESS_FINE_LOCATION permission")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Missing ACCESS_COARSE_LOCATION permission")
            return
        }

        Log.d(TAG, "Requesting location updates...")

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnSuccessListener {
            Log.d(TAG, "Location updates started successfully")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start location updates", e)
        }
    }

    /**
     * Stop receiving location updates
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Handle new location update
     * @param location New location from GPS
     */
    private fun onNewLocation(location: Location) {
        val newPoint = LatLng(location.latitude, location.longitude)

        // Calculate distance from last point using Haversine formula
        if (routePoints.isNotEmpty()) {
            val lastPoint = routePoints.last()
            val distance = calculateDistance(lastPoint, newPoint)
            totalDistance += distance
        }

        routePoints.add(newPoint)

        val duration = (System.currentTimeMillis() - startTime) / 1000
        notifyLocationUpdate(newPoint, totalDistance, duration)
        updateNotification(duration, totalDistance)
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * @param point1 First coordinate
     * @param point2 Second coordinate
     * @return Distance in meters
     */
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0 // meters
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLng = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Create initial notification for foreground service
     * @return Notification to display
     */
    private fun createNotification(): Notification {
        // Intent to open app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to_walk", true)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to stop walk
        val stopIntent = Intent("pt.ipp.estg.trabalho_cmu.STOP_WALK").apply {
            setPackage(packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.walk_in_progress_title, animalName))
            .setContentText(getString(R.string.walk_in_progress_text))
            .setSmallIcon(R.drawable.ic_walk_notification)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_stop,
                getString(R.string.walk_notification_stop),
                stopPendingIntent
            )
            .build()
    }


    /**
     * Update notification with current walk stats
     * @param duration Current duration in seconds
     * @param distance Current distance in meters
     */
    private fun updateNotification(duration: Long, distance: Double) {
        val distanceKm = distance / 1000.0
        val timeFormatted = formatDuration(duration)

        // Intent to open app
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to_walk", true)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to stop walk
        val stopIntent = Intent("pt.ipp.estg.trabalho_cmu.STOP_WALK").apply {
            setPackage(packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.walk_in_progress_title, animalName))
            .setContentText(
                getString(
                    R.string.walk_notification_stats,
                    timeFormatted,
                    String.format("%.2f", distanceKm)
                )
            )
            .setSmallIcon(R.drawable.ic_walk_notification)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_stop,
                getString(R.string.walk_notification_stop),
                stopPendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Format duration to HH:mm:ss
     * @param seconds Duration in seconds
     * @return Formatted string
     */
    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    /**
     * Add listener for walk updates
     * @param listener Listener to add
     */
    fun addListener(listener: WalkUpdateListener) {
        listeners.add(listener)
    }

    /**
     * Remove listener for walk updates
     * @param listener Listener to remove
     */
    fun removeListener(listener: WalkUpdateListener) {
        listeners.remove(listener)
    }

    /**
     * Notify all listeners of location update
     */
    private fun notifyLocationUpdate(location: LatLng, distance: Double, duration: Long) {
        listeners.forEach { it.onLocationUpdate(location, distance, duration) }
    }

    /**
     * Notify all listeners of tracking state change
     */
    private fun notifyTrackingStateChanged() {
        listeners.forEach { it.onTrackingStateChanged(isTracking) }
    }

    /**
     * Check if service is currently tracking
     * @return True if tracking is active
     */
    fun isCurrentlyTracking() = isTracking

    /**
     * Get current walk data while tracking
     * @return WalkData or null if not tracking
     */
    fun getCurrentWalkData(): WalkData? {
        if (!isTracking) return null
        val duration = (System.currentTimeMillis() - startTime) / 1000
        return WalkData(animalId, animalName, duration, totalDistance, routePoints.toList())
    }

    /**
     * Data class containing all walk tracking information
     * @property animalId ID of the animal
     * @property animalName Name of the animal
     * @property duration Total duration in seconds
     * @property distance Total distance in meters
     * @property routePoints List of GPS coordinates
     */
    data class WalkData(
        val animalId: String,
        val animalName: String,
        val duration: Long,      // seconds
        val distance: Double,    // meters
        val routePoints: List<LatLng>
    )
}
