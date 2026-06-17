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

    /**
     * Eases [current] toward [target] by [alpha] (0..1) along the shortest arc, so the needle
     * doesn't spin the long way around when the heading wraps past 0°/360°. Returns a value in
     * [0, 360).
     */
    fun smoothedTowards(current: Float, target: Float, alpha: Float): Float {
        // shortest signed delta in (-180, 180]
        val delta = (((target - current) % 360f) + 540f) % 360f - 180f
        return ((current + delta * alpha) % 360f + 360f) % 360f
    }

    private val CARDINALS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
}
