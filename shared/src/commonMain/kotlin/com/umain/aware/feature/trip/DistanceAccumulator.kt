package com.umain.aware.feature.trip

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Accumulates trip distance by summing the great-circle (haversine) distance between successive GPS
 * fixes. Pure (SRP): no Compose, no KSensor, no permission handling.
 */
class DistanceAccumulator {
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    var totalMeters: Double = 0.0
        private set

    /** Feed a new fix; the first fix only seeds the path, subsequent fixes add to the total. */
    fun onFix(lat: Double, lon: Double) {
        val pLat = lastLat
        val pLon = lastLon
        if (pLat != null && pLon != null) {
            totalMeters += haversine(pLat, pLon, lat, lon)
        }
        lastLat = lat
        lastLon = lon
    }

    fun reset() {
        lastLat = null
        lastLon = null
        totalMeters = 0.0
    }

    companion object {
        const val EARTH_RADIUS_M = 6_371_000.0

        private fun Double.toRadians() = this * PI / 180.0

        /** Great-circle distance between two lat/lon points, in metres. */
        fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLat = (lat2 - lat1).toRadians()
            val dLon = (lon2 - lon1).toRadians()
            val a = sin(dLat / 2).let { it * it } +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLon / 2).let { it * it }
            return 2.0 * EARTH_RADIUS_M * asin(min(1.0, sqrt(a)))
        }
    }
}
