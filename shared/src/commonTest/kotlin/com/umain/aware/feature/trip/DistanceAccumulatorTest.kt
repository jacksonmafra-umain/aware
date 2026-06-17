package com.umain.aware.feature.trip

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DistanceAccumulatorTest {

    @Test
    fun one_degree_of_longitude_at_equator_is_about_111km() {
        // Known value: 1° of longitude at the equator ≈ 111.19 km.
        val meters = DistanceAccumulator.haversine(0.0, 0.0, 0.0, 1.0)
        assertTrue(abs(meters - 111_195.0) < 100.0, "expected ~111195 m but was $meters")
    }

    @Test
    fun first_fix_does_not_add_distance() {
        val acc = DistanceAccumulator()
        acc.onFix(40.0, -3.0)
        assertEquals(0.0, acc.totalMeters)
    }

    @Test
    fun successive_fixes_accumulate() {
        val acc = DistanceAccumulator()
        acc.onFix(0.0, 0.0)
        acc.onFix(0.0, 1.0) // +~111195 m
        acc.onFix(0.0, 2.0) // +~111195 m
        val expected = 2 * DistanceAccumulator.haversine(0.0, 0.0, 0.0, 1.0)
        assertTrue(abs(acc.totalMeters - expected) < 1.0)
    }
}
