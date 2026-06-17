package com.umain.aware.feature.shake

import kotlin.math.sqrt

/**
 * Turns a stream of accelerometer samples into discrete "shake" events — the logic behind a
 * shake-to-report-a-bug gesture.
 *
 * Unit-independent by design: KSensor normalises the Android accelerometer by the sensor's
 * maximum range, so the raw magnitude is not in m/s². Instead of assuming a unit, the detector
 * keeps a low-pass *baseline* of the resting magnitude (gravity at rest) and fires when the
 * instantaneous magnitude exceeds that baseline by [thresholdGForce]. Because both numerator and
 * baseline are scaled the same way, the ratio is exactly the real g-multiple — correct whether the
 * samples are normalised or in m/s².
 *
 * Pure and platform-free (SRP): it holds only the baseline and cooldown clock and is driven
 * entirely by [onSample], so it unit-tests without Compose or KSensor.
 *
 * @param thresholdGForce how many times the resting magnitude counts as a shake (≈ g-force).
 * @param cooldownMs minimum gap between two counted shakes, so one shake doesn't fire repeatedly.
 */
class ShakeDetector(
    private val thresholdGForce: Float = 1.6f,
    private val cooldownMs: Long = 500L,
) {
    private var baseline: Float? = null
    private var lastShakeMs: Long? = null

    /** Feed one sample; returns true exactly when this sample registers a new shake. */
    fun onSample(x: Float, y: Float, z: Float, timestampMs: Long): Boolean {
        val magnitude = sqrt(x * x + y * y + z * z)

        val current = baseline
        if (current == null || current == 0f) {
            // First (or degenerate) sample only seeds the resting baseline.
            baseline = magnitude
            return false
        }

        val ratio = magnitude / current
        // Track the resting magnitude slowly so orientation changes don't count as shakes.
        baseline = current * 0.9f + magnitude * 0.1f

        val last = lastShakeMs
        if (ratio >= thresholdGForce && (last == null || timestampMs - last >= cooldownMs)) {
            lastShakeMs = timestampMs
            return true
        }
        return false
    }
}
