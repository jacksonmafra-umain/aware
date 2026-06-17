package com.umain.aware.feature.rotate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
 * Auto-rotate video: maps DEVICE_ORIENTATION to a player layout — landscape goes "fullscreen", any
 * portrait orientation stays inline. The orientation is matched by string so it is robust to the
 * library's exact enum names.
 */
@Composable
fun AutoRotateScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val types = remember { listOf(SensorType.DEVICE_ORIENTATION) }

    var orientation by remember { mutableStateOf("PORTRAIT") }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Orientation) orientation = r.name
            }
            is SensorUpdate.Error -> Unit
        }
    }

    val fullscreen = orientation.contains("landscape", ignoreCase = true)

    FeatureScaffold("Auto-rotate video", platform, onBack) {
        MetricCard {
            MetricText(if (fullscreen) "Fullscreen" else "Inline", "player mode")
            Text(
                "Detected orientation: $orientation",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        // A mock "video" surface that grows to fullscreen in landscape.
        Surface(
            modifier = Modifier.fillMaxWidth().height(if (fullscreen) 260.dp else 150.dp),
            color = Color(0xFF101418),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(if (fullscreen) "▶  Fullscreen" else "▶  Inline portrait")
            }
        }
    }
}
