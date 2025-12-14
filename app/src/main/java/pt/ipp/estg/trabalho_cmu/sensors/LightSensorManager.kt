package pt.ipp.estg.trabalho_cmu.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Manages the ambient light sensor and exposes data using LiveData.
 *
 * - lightLevel: current light level in lux
 * - shouldUseDarkTheme: whether the app should switch to dark mode
 */
class LightSensorManager(context: Context) : SensorEventListener {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    // Light level (lux)
    private val _lightLevel = MutableLiveData<Float>()
    val lightLevel: LiveData<Float> get() = _lightLevel

    // Dark theme toggle
    private val _shouldUseDarkTheme = MutableLiveData<Boolean>(false)
    val shouldUseDarkTheme: LiveData<Boolean> get() = _shouldUseDarkTheme

    // Threshold for theme switching
    var lightThreshold: Float = 100f

    val isSensorAvailable: Boolean get() = lightSensor != null

    fun startListening() {
        lightSensor?.let {
            sensorManager.registerListener(
                this, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val lux = event.values[0]

        _lightLevel.postValue(lux)

        val dark = lux < lightThreshold
        if (_shouldUseDarkTheme.value != dark) {
            _shouldUseDarkTheme.postValue(dark)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
