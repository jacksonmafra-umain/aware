package com.umain.aware.feature.trip

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.umain.aware.core.CollectSensors
import com.umain.aware.core.CollectStates
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.MetricText
import com.umain.aware.core.Reading
import com.umain.aware.core.RequestLocationPermission
import com.umain.aware.core.SensorSource
import com.umain.aware.core.SensorType
import com.umain.aware.core.SensorUpdate
import com.umain.aware.core.StateReading
import com.umain.aware.core.StateSource
import com.umain.aware.core.StateType
import com.umain.aware.core.StateUpdate
import com.umain.aware.core.StatusPill
import org.koin.compose.koinInject
import kotlin.math.roundToInt

/**
 * Trip tracker: requests location permission up front, sums distance between GPS fixes with a
 * [DistanceAccumulator] at a fixed 2s cadence, and surfaces a "GPS is off" warning sourced from the
 * LOCATION *state* (separate from the LOCATION *sensor*).
 */
@Composable
fun TripTrackerScreen(onBack: () -> Unit) {
    val sensorSource = koinInject<SensorSource>()
    val stateSource = koinInject<StateSource>()
    val accumulator = remember { DistanceAccumulator() }

    var granted by remember { mutableStateOf(false) }
    var totalMeters by remember { mutableStateOf(0.0) }
    var gpsOn by remember { mutableStateOf(true) }
    var platform by remember { mutableStateOf<String?>(null) }

    RequestLocationPermission { granted = it }

    CollectStates(stateSource, remember { listOf(StateType.LOCATION) }) { update ->
        if (update is StateUpdate.Data) {
            val r = update.reading
            if (r is StateReading.LocationStatus) gpsOn = r.isOn
        }
    }

    if (granted) {
        CollectSensors(sensorSource, remember { listOf(SensorType.LOCATION) }, intervalMs = 2_000L) { update ->
            if (update is SensorUpdate.Data) {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Location && r.lat != null && r.lon != null) {
                    accumulator.onFix(r.lat, r.lon)
                    totalMeters = accumulator.totalMeters
                }
            }
        }
    }

    val km = (totalMeters / 10.0).roundToInt() / 100.0

    FeatureScaffold("Trip tracker", platform, onBack) {
        MetricCard {
            MetricText("$km", "km travelled")
            StatusPill(if (gpsOn) "GPS on" else "GPS off", active = gpsOn)
            if (!granted) {
                Text(
                    "Location permission is needed to track your trip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            } else if (!gpsOn) {
                Text(
                    "GPS is off — turn on location services to keep tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
