package com.umain.aware.feature.compass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Compass: a [Heading] turns the magnetometer into degrees, and a needle points toward magnetic
 * north (north sits at angle −heading relative to the top of the device).
 */
@Composable
fun CompassScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val types = remember { listOf(SensorType.MAGNETOMETER) }

    var heading by remember { mutableStateOf(0f) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Magnetometer) heading = Heading.fromMagnetometer(r.x, r.y)
            }
            is SensorUpdate.Error -> Unit
        }
    }

    val needleColor = MaterialTheme.colorScheme.error
    val dialColor = MaterialTheme.colorScheme.outlineVariant

    FeatureScaffold("Compass", platform, onBack) {
        MetricCard {
            MetricText("${heading.roundToInt()}°", Heading.cardinal(heading))
            Canvas(modifier = Modifier.size(220.dp)) {
                val radius = min(size.width, size.height) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = dialColor, radius = radius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
                val theta = (-heading) * (kotlin.math.PI.toFloat() / 180f)
                val tip = Offset(
                    x = center.x + radius * 0.82f * sin(theta),
                    y = center.y - radius * 0.82f * cos(theta),
                )
                drawLine(color = needleColor, start = center, end = tip, strokeWidth = 10f)
                drawCircle(color = needleColor, radius = 10f, center = center)
            }
        }
    }
}
