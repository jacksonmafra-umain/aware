package com.umain.aware.feature.compass

import kotlin.test.Test
import kotlin.test.assertEquals

class HeadingTest {

    private fun assertDeg(expected: Float, actual: Float) =
        assertEquals(expected, actual, absoluteTolerance = 0.01f)

    @Test
    fun cardinal_axes_map_to_expected_degrees() {
        assertDeg(0f, Heading.fromMagnetometer(1f, 0f))
        assertDeg(90f, Heading.fromMagnetometer(0f, 1f))
        assertDeg(180f, Heading.fromMagnetometer(-1f, 0f))
        assertDeg(270f, Heading.fromMagnetometer(0f, -1f))
    }

    @Test
    fun result_is_always_normalised_to_0_360() {
        val h = Heading.fromMagnetometer(-1f, -0.0001f) // just past 180°, must not be negative
        assertEquals(true, h in 0f..360f)
    }

    @Test
    fun cardinal_labels_wrap_around() {
        assertEquals("N", Heading.cardinal(0f))
        assertEquals("N", Heading.cardinal(359f))
        assertEquals("E", Heading.cardinal(90f))
        assertEquals("S", Heading.cardinal(180f))
        assertEquals("W", Heading.cardinal(270f))
    }

    @Test
    fun smoothing_takes_the_shortest_arc_across_zero() {
        // From 350° toward 10° should move forward through 0° (to ~352°), not backward.
        val next = Heading.smoothedTowards(current = 350f, target = 10f, alpha = 0.5f)
        // shortest delta is +20°, half of that is +10° => 360 -> 0
        assertEquals(0f, next, absoluteTolerance = 0.01f)
        assertEquals(true, next in 0f..360f)
    }

    @Test
    fun smoothing_eases_partway_toward_target() {
        val next = Heading.smoothedTowards(current = 0f, target = 100f, alpha = 0.25f)
        assertEquals(25f, next, absoluteTolerance = 0.01f)
    }

    @Test
    fun tilt_compensated_flat_device_reads_field_direction_as_heading() {
        val g = 9.81f // gravity on +z (device flat, face up)
        // Field pointing to the device's top (+y) => facing north => 0°.
        assertDeg(0f, Heading.tiltCompensated(0f, 0f, g, 0f, 30f, 0f)!!)
        // Field to the right (+x) => north is to the right => facing west => 270°.
        assertDeg(270f, Heading.tiltCompensated(0f, 0f, g, 30f, 0f, 0f)!!)
        // Field to the bottom (-y) => facing south => 180°.
        assertDeg(180f, Heading.tiltCompensated(0f, 0f, g, 0f, -30f, 0f)!!)
        // Field to the left (-x) => 90°.
        assertDeg(90f, Heading.tiltCompensated(0f, 0f, g, -30f, 0f, 0f)!!)
    }

    @Test
    fun tilt_compensated_is_stable_when_pitched() {
        // Facing north: gravity on +z, field with north (+y) and downward (-z) components.
        val flat = Heading.tiltCompensated(0f, 0f, 9.81f, 0f, 20f, -40f)!!
        // Pitch the whole device forward 45° about its x-axis: rotate BOTH vectors the same way.
        val pitched = Heading.tiltCompensated(0f, -6.937f, 6.937f, 0f, 42.43f, -14.14f)!!
        assertDeg(0f, flat)
        // Heading stays ~north despite the pitch (raw atan2 would swing wildly).
        val delta = kotlin.math.abs(((pitched - flat + 540f) % 360f) - 180f)
        assertEquals(true, delta < 5f, "pitch changed heading by $delta°")
    }

    @Test
    fun tilt_compensated_returns_null_in_free_fall() {
        assertEquals(null, Heading.tiltCompensated(0f, 0f, 0f, 0f, 30f, 0f))
    }
}
