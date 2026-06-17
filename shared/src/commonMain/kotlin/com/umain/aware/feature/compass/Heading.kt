package com.umain.aware.feature.compass

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

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

    /**
     * Tilt-compensated heading in degrees (0 = north, 90 = east), built from the accelerometer
     * (gravity) and magnetometer using the standard rotation-matrix method — the same maths as
     * Android's `getRotationMatrix` + `getOrientation`. Works at any device tilt, unlike
     * [fromMagnetometer]. Returns null for degenerate input (free-fall, or pointing straight at a
     * magnetic pole). Inputs must share one right-handed device frame with gravity positive; if a
     * platform delivers the accelerometer or magnetometer with a flipped sign, flip it before
     * calling.
     */
    fun tiltCompensated(
        ax: Float, ay: Float, az: Float,
        mx: Float, my: Float, mz: Float,
    ): Float? {
        val aNorm = sqrt(ax * ax + ay * ay + az * az)
        if (aNorm < 1e-6f) return null
        val axN = ax / aNorm
        val ayN = ay / aNorm
        val azN = az / aNorm

        // East = magnetic field x gravity
        var hx = my * azN - mz * ayN
        var hy = mz * axN - mx * azN
        var hz = mx * ayN - my * axN
        val hNorm = sqrt(hx * hx + hy * hy + hz * hz)
        if (hNorm < 1e-6f) return null
        hx /= hNorm; hy /= hNorm; hz /= hNorm

        // North = gravity x East
        val my2 = azN * hx - axN * hz
        val deg = atan2(hy, my2) * (180f / PI.toFloat())
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
