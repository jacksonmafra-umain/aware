package com.umain.aware.feature.shake

import kotlin.math.sqrt

/**
 * Turns a stream of accelerometer samples into discrete "shake" events — the logic behind a
 * shake-to-report-a-bug gesture.
 *
 * Pure and platform-free (SRP): it holds only the cooldown clock and is driven entirely by
 * [onSample], so it can be unit-tested without Compose or KSensor. Samples are assumed to be in
 * m/s² (gravity ≈ 9.80665); magnitude is converted to g and gravity (1 g) subtracted, so a phone at
 * rest reads ~0.
 *
 * @param thresholdG how many g above rest counts as a shake.
 * @param cooldownMs minimum gap between two counted shakes, to avoid one shake firing repeatedly.
 */
class ShakeDetector(
    private val thresholdG: Float = 2.7f,
    private val cooldownMs: Long = 500L,
) {
    private var lastShakeMs: Long? = null

    /** Feed one sample; returns true exactly when this sample registers a new shake. */
    fun onSample(x: Float, y: Float, z: Float, timestampMs: Long): Boolean {
        val gForce = sqrt(x * x + y * y + z * z) / GRAVITY
        val excess = gForce - 1f
        val last = lastShakeMs
        if (excess > thresholdG && (last == null || timestampMs - last >= cooldownMs)) {
            lastShakeMs = timestampMs
            return true
        }
        return false
    }

    companion object {
        const val GRAVITY = 9.80665f
    }
}
