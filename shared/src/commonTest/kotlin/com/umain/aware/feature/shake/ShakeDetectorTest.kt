package com.umain.aware.feature.shake

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShakeDetectorTest {

    // A steady resting magnitude (the actual unit is irrelevant — detection is ratio-based).
    private val rest = 9.80665f

    @Test
    fun first_sample_only_seeds_the_baseline() {
        val d = ShakeDetector(thresholdGForce = 1.8f, cooldownMs = 500L)
        assertFalse(d.onSample(0f, 0f, rest, 0L))
    }

    @Test
    fun holding_still_is_not_a_shake() {
        val d = ShakeDetector(thresholdGForce = 1.8f, cooldownMs = 500L)
        d.onSample(0f, 0f, rest, 0L)
        assertFalse(d.onSample(0f, 0f, rest, 100L))
        assertFalse(d.onSample(0f, 0f, rest * 1.1f, 200L)) // minor noise stays below threshold
    }

    @Test
    fun jolt_well_above_baseline_is_a_shake() {
        val d = ShakeDetector(thresholdGForce = 1.8f, cooldownMs = 500L)
        d.onSample(0f, 0f, rest, 0L)            // baseline
        assertTrue(d.onSample(0f, 0f, rest * 2.5f, 100L)) // 2.5x resting magnitude
    }

    @Test
    fun second_shake_inside_cooldown_is_ignored_then_allowed_after() {
        val d = ShakeDetector(thresholdGForce = 1.8f, cooldownMs = 500L)
        d.onSample(0f, 0f, rest, 0L)
        val jolt = rest * 3f
        assertTrue(d.onSample(0f, 0f, jolt, 100L))
        assertFalse(d.onSample(0f, 0f, jolt, 300L), "within cooldown")
        assertTrue(d.onSample(0f, 0f, jolt, 700L), "after cooldown")
    }
}
