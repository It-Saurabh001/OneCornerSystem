package com.saurabh.onecornersystem.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {
    private const val EARTH_RADIUS_KM = 6371.0 // Earth's radius in kilometers
    private const val TAG = "LocationUtils"


    /**
     * Calculate distance between two points using Haversine formula
     * @return Distance in kilometers
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculate distance in meters
     */
    fun calculateDistanceInMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        return calculateDistance(lat1, lon1, lat2, lon2) * 1000
    }

    /**
     * Check if a location is within radius
     */
    fun isWithinRadius(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        radiusKm: Double
    ): Boolean {
        return calculateDistance(lat1, lon1, lat2, lon2) <= radiusKm
    }

    /**
     * Format distance for display
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1 -> "${(distanceKm * 1000).toInt()} m away"
            distanceKm < 10 -> "${String.format("%.1f", distanceKm)} km away"
            else -> "${distanceKm.toInt()} km away"
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get last known location using LocationManager
     */
    fun getCurrentLocation(context: Context): Location? {
        // First check permission
        if (!hasLocationPermission(context)) {
            return null
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Check if GPS or Network is enabled
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                return null
            }

            // Try to get location from Network first (faster, less accurate)
            var location: Location? = null
            if (isNetworkEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            // If Network location is null, try GPS (slower, more accurate)
            if (location == null && isGpsEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            location
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get current location using FusedLocationProvider (more accurate)
     * This is a suspend function - use in coroutines
     */
    suspend fun getCurrentLocationFused(context: Context): Location? {
        if (!hasLocationPermission(context)) {
            return null
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.await()
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if location is enabled (GPS or Network)
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get fresh current location using FusedLocationProvider's getCurrentLocation
     * This fetches a NEW location instead of cached lastLocation
     * Use this when you need accurate current position
     */
    @SuppressLint("MissingPermission")
    suspend fun getFreshCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) {
            Log.d(TAG, "No location permission")
            return null
        }

        if (!isLocationEnabled(context)) {
            Log.d(TAG, "Location is disabled")
            return null
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(TAG, "Fresh location: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        // Fallback to lastLocation if getCurrentLocation returns null
                        Log.d(TAG, "getCurrentLocation returned null, trying lastLocation")
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { lastLocation ->
                                Log.d(TAG, "Last location: ${lastLocation?.latitude}, ${lastLocation?.longitude}")
                                continuation.resume(lastLocation)
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "Failed to get lastLocation", it)
                                continuation.resume(null)
                            }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get current location", e)
                    // Fallback to lastLocation on failure
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            continuation.resume(lastLocation)
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getFreshCurrentLocation", e)
            null
        }
    }

    /**
     * Check if location settings are satisfied and provide the IntentSender for resolution
     */
    fun checkLocationSettings(
        context: Context,
        onResolutionRequired: (IntentSender) -> Unit,
        onAlreadySatisfied: () -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onAlreadySatisfied()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    onResolutionRequired(exception.resolution.intentSender)
                } catch (e: Exception) {
                    Log.e(TAG, "Error with location resolution", e)
                }
            }
        }
    }

}