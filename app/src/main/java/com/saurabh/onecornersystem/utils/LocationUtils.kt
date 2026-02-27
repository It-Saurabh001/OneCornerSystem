package com.saurabh.onecornersystem.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {
    private const val EARTH_RADIUS_KM = 6371.0 // Earth's radius in kilometers


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

}