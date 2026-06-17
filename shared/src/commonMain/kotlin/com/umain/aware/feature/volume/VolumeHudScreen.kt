package com.umain.aware.feature.volume

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.aware.core.CollectStates
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.MetricText
import com.umain.aware.core.StateReading
import com.umain.aware.core.StateSource
import com.umain.aware.core.StateType
import com.umain.aware.core.StateUpdate
import org.koin.compose.koinInject

/**
 * Volume HUD: a custom overlay bar that tracks the system volume, with a "too loud" hint past 80%.
 *
 * KSensor reports volume inconsistently across platforms: on iOS the value is already a 0–100
 * percentage, while on Android it's the raw STREAM_MUSIC index (e.g. 7 of ~15) with no maximum
 * exposed. So we interpret it per platform — iOS as a percentage, Android normalised against the
 * largest index seen (floored at 15) — to get a comparable percentage on both.
 */
@Composable
fun VolumeHudScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.VOLUME) }

    var raw by remember { mutableStateOf(0) }
    var maxIndex by remember { mutableStateOf(15) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            val r = update.reading
            if (r is StateReading.Volume) {
                raw = r.percent
                if (r.percent > maxIndex) maxIndex = r.percent
            }
        }
    }

    val isIos = platform?.contains("ios", ignoreCase = true) == true
    val percent = if (isIos) {
        raw.coerceIn(0, 100)
    } else {
        (raw * 100 / maxIndex.coerceAtLeast(1)).coerceIn(0, 100)
    }
    val fill = percent / 100f
    val tooLoud = percent > 80
    val barColor = if (tooLoud) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    FeatureScaffold("Volume HUD", platform, onBack) {
        MetricCard {
            MetricText("$percent%", "system volume")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(trackColor, MaterialTheme.shapes.large),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .fillMaxHeight()
                        .background(barColor, MaterialTheme.shapes.large),
                )
            }
            if (tooLoud) {
                Text(
                    "Too loud — high volume for long periods can damage hearing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
