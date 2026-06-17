package com.umain.aware.feature.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.aware.core.ChargingState
import com.umain.aware.core.CollectStates
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.MetricText
import com.umain.aware.core.StateReading
import com.umain.aware.core.StateSource
import com.umain.aware.core.StateType
import com.umain.aware.core.StateUpdate
import com.umain.aware.core.StatusPill
import org.koin.compose.koinInject

/**
 * Battery saver: feeds the BATTERY state into [BatteryPolicy] and reflects its three decisions —
 * saver mode, deferred sync while charging, and an overheat warning.
 */
@Composable
fun BatterySaverScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.BATTERY) }

    var battery by remember { mutableStateOf<StateReading.Battery?>(null) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            val r = update.reading
            if (r is StateReading.Battery) battery = r
        }
    }

    val b = battery
    val decision = b?.let { BatteryPolicy.decide(it.levelPercent, it.charging, it.temperatureC) }

    FeatureScaffold("Battery saver", platform, onBack) {
        MetricCard {
            MetricText(b?.levelPercent?.let { "$it%" } ?: "—", "battery level")
            StatusPill(
                text = if (b?.charging == ChargingState.CHARGING || b?.charging == ChargingState.FULL) "Charging" else "On battery",
                active = b?.charging == ChargingState.CHARGING || b?.charging == ChargingState.FULL,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill("Saver", active = decision?.saverOn == true)
                StatusPill("Sync", active = decision?.runDeferredSync == true)
            }
            if (decision?.overheatWarning == true) {
                Text(
                    "Battery is hot (${b?.temperatureC?.let { it.toInt() } ?: "?"} °C) — pausing heavy work.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
