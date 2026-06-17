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
 * KSensor reports the raw STREAM_MUSIC index (e.g. 7), not a percentage, and doesn't expose the
 * stream maximum. Most devices use a max of 15, but some use more, so we normalise against
 * `max(15, highest value seen)` — which self-corrects upward as the user reaches their device's
 * real maximum.
 */
@Composable
fun VolumeHudScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.VOLUME) }

    var level by remember { mutableStateOf(0) }
    var maxLevel by remember { mutableStateOf(15) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            val r = update.reading
            if (r is StateReading.Volume) {
                level = r.percent
                if (r.percent > maxLevel) maxLevel = r.percent
            }
        }
    }

    val percent = if (maxLevel > 0) (level * 100 / maxLevel).coerceIn(0, 100) else 0
    val fill = (level.toFloat() / maxLevel).coerceIn(0f, 1f)
    val tooLoud = percent > 80
    val barColor = if (tooLoud) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    FeatureScaffold("Volume HUD", platform, onBack) {
        MetricCard {
            MetricText("$percent%", "system volume (level $level of $maxLevel)")
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
