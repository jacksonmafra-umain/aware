package com.umain.aware.feature.tilt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarbleBoxTest {

    @Test
    fun starts_centered() {
        val m = MarbleBox()
        assertEquals(0.5f, m.x)
        assertEquals(0.5f, m.y)
    }

    @Test
    fun positive_tilt_rolls_right_and_down() {
        val m = MarbleBox()
        repeat(20) { m.onTilt(tiltX = 1f, tiltY = 1f, dtSeconds = 0.016f) }
        assertTrue(m.x > 0.5f, "expected marble to move right, was ${m.x}")
        assertTrue(m.y > 0.5f, "expected marble to move down, was ${m.y}")
    }

    @Test
    fun never_leaves_the_box() {
        val m = MarbleBox()
        repeat(500) { m.onTilt(tiltX = 5f, tiltY = -5f, dtSeconds = 0.05f) }
        assertTrue(m.x in 0f..1f, "x out of bounds: ${m.x}")
        assertTrue(m.y in 0f..1f, "y out of bounds: ${m.y}")
    }

    @Test
    fun reset_recenters_and_stops() {
        val m = MarbleBox()
        repeat(10) { m.onTilt(1f, 1f, 0.05f) }
        m.reset()
        assertEquals(0.5f, m.x)
        assertEquals(0.5f, m.y)
    }
}
