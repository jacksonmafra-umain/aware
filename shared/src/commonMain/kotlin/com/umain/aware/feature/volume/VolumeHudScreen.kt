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
 * Volume HUD: a custom overlay bar that tracks the system volume, with a "too loud" hint once it
 * passes 80% — the kind of hearing-safety nudge media apps show.
 */
@Composable
fun VolumeHudScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.VOLUME) }

    var percent by remember { mutableStateOf(0) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            val r = update.reading
            if (r is StateReading.Volume) percent = r.percent
        }
    }

    val tooLoud = percent > 80
    val fill = (percent.coerceIn(0, 100)) / 100f
    val barColor = if (tooLoud) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    FeatureScaffold("Volume HUD", platform, onBack) {
        MetricCard {
            MetricText("$percent%", "system volume")
            // Custom HUD bar.
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
