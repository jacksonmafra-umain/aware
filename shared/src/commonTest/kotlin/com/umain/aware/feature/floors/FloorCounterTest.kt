package com.umain.aware.feature.floors

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FloorCounterTest {

    @Test
    fun altitude_is_zero_at_sea_level_reference() {
        assertTrue(abs(FloorCounter.altitudeMeters(1013.25f)) < 0.01f)
    }

    @Test
    fun first_reading_is_zero_floors() {
        val c = FloorCounter()
        c.onPressure(1013.25f)
        assertEquals(0, c.floors)
    }

    @Test
    fun lower_pressure_counts_floors_up() {
        val c = FloorCounter(metersPerFloor = 3f)
        c.onPressure(1013.25f) // baseline at "ground"
        c.onPressure(1000f)    // lower pressure -> higher up
        val expected = (FloorCounter.altitudeMeters(1000f) / 3f).roundToInt()
        assertEquals(expected, c.floors)
        assertTrue(c.floors > 0)
    }

    @Test
    fun higher_pressure_counts_floors_down() {
        val c = FloorCounter(metersPerFloor = 3f)
        c.onPressure(1000f)    // baseline up high
        c.onPressure(1013.25f) // higher pressure -> descended
        assertTrue(c.floors < 0)
    }
}
