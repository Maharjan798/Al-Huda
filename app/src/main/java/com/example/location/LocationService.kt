package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class UserLocationResult(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentUserLocation(): UserLocationResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext UserLocationResult(
                latitude = 21.4225,
                longitude = 39.8262,
                cityName = "Makkah, Saudi Arabia",
                isSuccess = false,
                errorMessage = "Location permission not granted"
            )
        }

        try {
            val location = fetchLastKnownOrFreshLocation()
            if (location != null) {
                val city = resolveCityName(location.latitude, location.longitude)
                UserLocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = city,
                    isSuccess = true
                )
            } else {
                UserLocationResult(
                    latitude = 21.4225,
                    longitude = 39.8262,
                    cityName = "Makkah, Saudi Arabia",
                    isSuccess = false,
                    errorMessage = "Unable to obtain location fix"
                )
            }
        } catch (e: Exception) {
            UserLocationResult(
                latitude = 21.4225,
                longitude = 39.8262,
                cityName = "Makkah, Saudi Arabia",
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Location request failed"
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLastKnownOrFreshLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { freshLocation ->
            if (freshLocation != null) {
                if (continuation.isActive) continuation.resume(freshLocation)
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (continuation.isActive) continuation.resume(lastLocation)
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }.addOnFailureListener {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                if (continuation.isActive) continuation.resume(lastLocation)
            }.addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
        }

        continuation.invokeOnCancellation {
            cancellationTokenSource.cancel()
        }
    }

    fun resolveCityName(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: address.adminArea
                val country = address.countryName
                if (city != null && country != null) {
                    "$city, $country"
                } else city ?: country ?: String.format(Locale.US, "%.2f, %.2f", lat, lng)
            } else {
                String.format(Locale.US, "%.2f, %.2f", lat, lng)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "%.2f, %.2f", lat, lng)
        }
    }
}
