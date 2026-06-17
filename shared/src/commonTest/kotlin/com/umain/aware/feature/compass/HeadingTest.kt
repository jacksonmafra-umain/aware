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
}
