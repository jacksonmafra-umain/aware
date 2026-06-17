package com.umain.aware.feature.battery

import com.umain.aware.core.ChargingState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatteryPolicyTest {

    @Test
    fun low_and_not_charging_enables_saver() {
        val d = BatteryPolicy.decide(levelPercent = 15, charging = ChargingState.DISCHARGING, temperatureC = 25f)
        assertTrue(d.saverOn)
        assertFalse(d.runDeferredSync)
        assertFalse(d.overheatWarning)
    }

    @Test
    fun charging_runs_deferred_sync_and_no_saver_even_when_low() {
        val d = BatteryPolicy.decide(levelPercent = 15, charging = ChargingState.CHARGING, temperatureC = 25f)
        assertFalse(d.saverOn)
        assertTrue(d.runDeferredSync)
    }

    @Test
    fun hot_battery_warns() {
        val d = BatteryPolicy.decide(levelPercent = 80, charging = ChargingState.DISCHARGING, temperatureC = 45f)
        assertTrue(d.overheatWarning)
        assertFalse(d.saverOn)
    }

    @Test
    fun unknown_level_does_not_enable_saver() {
        val d = BatteryPolicy.decide(levelPercent = null, charging = ChargingState.DISCHARGING, temperatureC = null)
        assertEquals(BatteryDecision(saverOn = false, runDeferredSync = false, overheatWarning = false), d)
    }
}
