package com.umain.aware.feature.light

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeDeciderTest {

    @Test
    fun goes_dark_only_below_low_threshold() {
        val d = ThemeDecider(darkBelowLux = 10f, lightAboveLux = 50f)
        assertFalse(d.onLux(30f), "between thresholds, starts light, stays light")
        assertTrue(d.onLux(5f), "below low threshold -> dark")
    }

    @Test
    fun hysteresis_holds_between_thresholds() {
        val d = ThemeDecider(darkBelowLux = 10f, lightAboveLux = 50f)
        d.onLux(5f)                 // -> dark
        assertTrue(d.onLux(30f), "between thresholds -> stays dark (no flicker)")
        assertFalse(d.onLux(60f), "above high threshold -> light")
        assertFalse(d.onLux(30f), "between thresholds -> stays light")
    }
}
