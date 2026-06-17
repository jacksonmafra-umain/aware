package com.umain.aware.feature.shake

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShakeDetectorTest {

    @Test
    fun rest_at_one_g_is_not_a_shake() {
        val d = ShakeDetector(thresholdG = 2.0f, cooldownMs = 500L)
        // ~9.8 m/s² straight down == 1 g, excess ≈ 0.
        assertFalse(d.onSample(0f, 0f, 9.80665f, 0L))
    }

    @Test
    fun strong_jolt_above_threshold_is_a_shake() {
        val d = ShakeDetector(thresholdG = 2.0f, cooldownMs = 500L)
        // ~4 g jolt -> excess ≈ 3 g > 2 g.
        assertTrue(d.onSample(0f, 0f, 4f * 9.80665f, 0L))
    }

    @Test
    fun second_shake_inside_cooldown_is_ignored_then_allowed_after() {
        val d = ShakeDetector(thresholdG = 2.0f, cooldownMs = 500L)
        val jolt = 4f * 9.80665f
        assertTrue(d.onSample(0f, 0f, jolt, 0L))
        assertFalse(d.onSample(0f, 0f, jolt, 200L), "within cooldown")
        assertTrue(d.onSample(0f, 0f, jolt, 700L), "after cooldown")
    }
}
