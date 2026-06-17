package com.umain.aware.feature.battery

import com.umain.aware.core.ChargingState

/** What a power-aware app would do given the current battery state. */
data class BatteryDecision(
    val saverOn: Boolean,
    val runDeferredSync: Boolean,
    val overheatWarning: Boolean,
)

/**
 * Battery saver decisions: turn the saver on below 20% when not charging, run deferred sync work
 * while charging, and warn above 40 °C. Depends only on the Aware-owned [ChargingState] abstraction,
 * not on KSensor. Pure (SRP).
 */
object BatteryPolicy {
    fun decide(levelPercent: Int?, charging: ChargingState, temperatureC: Float?): BatteryDecision {
        val isCharging = charging == ChargingState.CHARGING || charging == ChargingState.FULL
        val saver = levelPercent != null && levelPercent < 20 && !isCharging
        val overheat = temperatureC != null && temperatureC > 40f
        return BatteryDecision(saverOn = saver, runDeferredSync = isCharging, overheatWarning = overheat)
    }
}
