package com.umain.aware.feature.steps

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StepSessionTest {

    @Test
    fun first_reading_is_baselined_to_zero() {
        val s = StepSession(goal = 1000)
        s.onCounter(5000) // device booted long ago; this is the baseline
        assertEquals(0, s.stepsSinceStart)
        assertEquals(0f, s.progress)
    }

    @Test
    fun counts_steps_since_baseline() {
        val s = StepSession(goal = 1000)
        s.onCounter(5000)
        s.onCounter(5250)
        assertEquals(250, s.stepsSinceStart)
        assertEquals(0.25f, s.progress)
        assertFalse(s.goalReached)
    }

    @Test
    fun reports_goal_reached() {
        val s = StepSession(goal = 1000)
        s.onCounter(5000)
        s.onCounter(6200)
        assertEquals(1200, s.stepsSinceStart)
        assertEquals(1f, s.progress) // clamped
        assertTrue(s.goalReached)
    }
}
