package com.umain.aware.feature.compass

import kotlin.math.PI
import kotlin.math.atan2

/**
 * Converts a magnetometer reading into a compass heading in degrees, normalised to [0, 360).
 * Pure (SRP).
 */
object Heading {

    /** Heading in degrees from the horizontal magnetic field components. */
    fun fromMagnetometer(x: Float, y: Float): Float {
        val deg = atan2(y, x) * (180f / PI.toFloat())
        return (deg + 360f) % 360f
    }

    /** Nearest 8-point cardinal label for a heading in degrees. */
    fun cardinal(degrees: Float): String {
        val index = (((degrees % 360f) + 360f + 22.5f) % 360f / 45f).toInt()
        return CARDINALS[index % 8]
    }

    private val CARDINALS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
}
