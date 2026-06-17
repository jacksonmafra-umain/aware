package com.umain.aware.feature.floors

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.umain.aware.core.CollectSensors
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.MetricText
import com.umain.aware.core.Reading
import com.umain.aware.core.SensorSource
import com.umain.aware.core.SensorType
import com.umain.aware.core.SensorUpdate
import org.koin.compose.koinInject

/**
 * Floors climbed: a [FloorCounter] turns barometric pressure into floors relative to the first
 * reading taken when the screen opened.
 */
@Composable
fun FloorClimbScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val counter = remember { FloorCounter() }
    val types = remember { listOf(SensorType.BAROMETER) }

    var floors by remember { mutableStateOf(0) }
    var pressure by remember { mutableStateOf<Float?>(null) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Barometer) {
                    counter.onPressure(r.pressure)
                    floors = counter.floors
                    pressure = r.pressure
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    FeatureScaffold("Floors climbed", platform, onBack) {
        MetricCard {
            MetricText("$floors", "floors since you started")
            Text(
                pressure?.let { "Current pressure: ${(it * 10).toInt() / 10.0} hPa" }
                    ?: "Waiting for the barometer…",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
