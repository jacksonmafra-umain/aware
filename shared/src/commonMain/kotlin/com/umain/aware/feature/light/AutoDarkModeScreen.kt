package com.umain.aware.feature.light

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlin.math.roundToInt

/**
 * Auto dark mode: a [ThemeDecider] applies lux thresholds with hysteresis and recolours a reader
 * pane between a light and a dark scheme.
 */
@Composable
fun AutoDarkModeScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val decider = remember { ThemeDecider() }
    val types = remember { listOf(SensorType.LIGHT) }

    var lux by remember { mutableStateOf(0f) }
    var dark by remember { mutableStateOf(false) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Light) {
                    lux = r.lux
                    dark = decider.onLux(r.lux)
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    FeatureScaffold("Auto dark mode", platform, onBack) {
        MetricCard {
            MetricText("${lux.roundToInt()} lx", if (dark) "dark theme" else "light theme")
        }
        // The reader pane recolours itself from the decision.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (dark) Color(0xFF121212) else Color(0xFFFDFDF6),
            contentColor = if (dark) Color(0xFFE6E6E6) else Color(0xFF1A1A1A),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                "Cover the light sensor to dim the page, or shine a light on it to brighten. " +
                    "A gap between the thresholds keeps the page from flickering when the light is borderline.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}
