package com.umain.aware.feature.pocket

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
import com.umain.aware.core.StatusPill
import org.koin.compose.koinInject

/**
 * Pocket mode: when the proximity sensor reports something near (a hand, an ear, a pocket),
 * playback pauses — the cover-to-pause / raise-to-ear behaviour of media and call apps.
 */
@Composable
fun PocketModeScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val types = remember { listOf(SensorType.PROXIMITY) }

    var near by remember { mutableStateOf(false) }
    var distanceCm by remember { mutableStateOf<Float?>(null) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Proximity) {
                    near = r.isNear
                    distanceCm = r.distanceCm
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    FeatureScaffold("Pocket mode", platform, onBack) {
        MetricCard {
            MetricText(if (near) "Paused" else "Playing", "playback")
            StatusPill(if (near) "Covered" else "Clear", active = near)
            Text(
                distanceCm?.let { "Proximity: ${it} cm" } ?: "Cover the top of the phone to pause.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
