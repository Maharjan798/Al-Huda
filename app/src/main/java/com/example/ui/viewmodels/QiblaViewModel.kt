package com.example.ui.viewmodels

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PrayerRepository
import com.example.location.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QiblaUiState(
    val compassAzimuthDegrees: Float = 0f,
    val targetQiblaAngle: Double = 251.2, // Default Makkah direction
    val deltaDegrees: Float = 0f,
    val isAligned: Boolean = false
)

class QiblaViewModel(
    application: Application,
    private val prayerRepository: PrayerRepository
) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _qiblaUiState = MutableStateFlow(QiblaUiState())
    val qiblaUiState: StateFlow<QiblaUiState> = _qiblaUiState.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val R = FloatArray(9)
    private val I = FloatArray(9)
    private val orientation = FloatArray(3)

    private val locationService = LocationService(application)

    private var userLat = 21.4225
    private var userLng = 39.8262

    init {
        if (locationService.hasLocationPermission()) {
            refreshLocationWithGPS()
        } else {
            fetchQiblaAngle(userLat, userLng)
        }
    }

    fun refreshLocationWithGPS() {
        viewModelScope.launch {
            if (locationService.hasLocationPermission()) {
                val result = locationService.getCurrentUserLocation()
                if (result.isSuccess) {
                    userLat = result.latitude
                    userLng = result.longitude
                    fetchQiblaAngle(result.latitude, result.longitude)
                }
            }
        }
    }

    fun fetchQiblaAngle(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
        viewModelScope.launch {
            val qiblaAngle = prayerRepository.getQiblaAngle(lat, lng)
            val current = _qiblaUiState.value
            _qiblaUiState.value = current.copy(targetQiblaAngle = qiblaAngle)
        }
    }

    fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
        }

        val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
        if (success) {
            SensorManager.getOrientation(R, orientation)
            var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuth < 0) azimuth += 360f

            val targetQibla = _qiblaUiState.value.targetQiblaAngle.toFloat()
            var delta = (targetQibla - azimuth) % 360
            if (delta < -180) delta += 360
            if (delta > 180) delta -= 360

            val isAligned = Math.abs(delta) < 5f

            _qiblaUiState.value = _qiblaUiState.value.copy(
                compassAzimuthDegrees = azimuth,
                deltaDegrees = delta,
                isAligned = isAligned
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        unregisterSensors()
    }
}
