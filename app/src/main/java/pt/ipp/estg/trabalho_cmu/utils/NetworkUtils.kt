package pt.ipp.estg.trabalho_cmu.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility object responsible for determining the network connectivity status
 * of the device. This helper must be initialized with the application context
 * before use.
 *
 * Usage:
 * - Call [init] once in the Application class.
 * - Use [isConnected] anywhere to check if the device currently has internet access.
 */
object NetworkUtils {

    private lateinit var appContext: Context

    /**
     * Initializes the utility with the application context.
     * Must be called before any other function.
     *
     * @param context Context used to access system network services.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Checks whether the device currently has an active internet connection.
     *
     * This method verifies active network capabilities such as:
     * - Wi-Fi
     * - Mobile data (cellular)
     * - Ethernet
     *
     * @return True if the device is connected to a network capable of internet access.
     */
    fun isConnected(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false

        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Check if network connection is available
     * @param context Application context
     * @return True if network is available, false otherwise
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo?.isConnected == true
        }
    }
}
