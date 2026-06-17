package com.umain.aware.feature.shake

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
import kotlin.time.TimeSource

/**
 * Shake to report: each detected shake files a "bug report". The screen owns no sensor logic — it
 * injects the [SensorSource] abstraction, feeds accelerometer samples to [ShakeDetector], and
 * renders the count. A monotonic clock supplies timestamps so the cooldown is robust regardless of
 * the (undocumented-unit) timestamp KSensor attaches.
 */
@Composable
fun ShakeToReportScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val detector = remember { ShakeDetector() }
    val clock = remember { TimeSource.Monotonic.markNow() }
    val types = remember { listOf(SensorType.ACCELEROMETER) }

    var reports by remember { mutableStateOf(0) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Accelerometer) {
                    val now = clock.elapsedNow().inWholeMilliseconds
                    if (detector.onSample(r.x, r.y, r.z, now)) reports++
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    FeatureScaffold("Shake to report", platform, onBack) {
        MetricCard {
            MetricText(reports.toString(), if (reports == 1) "report filed" else "reports filed")
            Text(
                "Give your phone a firm shake to file a bug report. A short cooldown keeps one shake from filing several.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
