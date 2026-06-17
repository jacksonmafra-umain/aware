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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Compass: a [Heading] turns the magnetometer into a heading; the dial (with N/E/S/W ticks and a
 * red north marker) rotates so north always points to magnetic north, while a fixed index at the
 * top marks the direction you're facing. The heading is eased along the shortest arc to remove
 * jitter and avoid spinning the long way around 0°/360°.
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
                if (r is Reading.Magnetometer) {
                    val target = Heading.fromMagnetometer(r.x, r.y)
                    heading = Heading.smoothedTowards(heading, target, alpha = 0.15f)
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    val tickColor = MaterialTheme.colorScheme.outline
    val northColor = MaterialTheme.colorScheme.error
    val indexColor = MaterialTheme.colorScheme.primary
    val hubColor = MaterialTheme.colorScheme.onSurfaceVariant

    FeatureScaffold("Compass", platform, onBack) {
        MetricCard {
            MetricText("${heading.roundToInt()}°", Heading.cardinal(heading))
            Canvas(modifier = Modifier.size(240.dp)) {
                val radius = min(size.width, size.height) / 2f - 6f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Outer ring.
                drawCircle(color = tickColor, radius = radius, center = center, style = Stroke(width = 4f))

                // Rotating dial: ticks every 30°, longer at the cardinals, red wedge at North.
                rotate(degrees = -heading, pivot = center) {
                    for (i in 0 until 12) {
                        val angle = i * 30f * (PI.toFloat() / 180f)
                        val isCardinal = i % 3 == 0
                        val tickLen = if (isCardinal) 22f else 12f
                        val outer = Offset(center.x + radius * sin(angle), center.y - radius * cos(angle))
                        val inner = Offset(
                            center.x + (radius - tickLen) * sin(angle),
                            center.y - (radius - tickLen) * cos(angle),
                        )
                        drawLine(color = tickColor, start = inner, end = outer, strokeWidth = if (isCardinal) 5f else 2f)
                    }
                    // North marker: a filled triangle at the top of the dial.
                    val tip = Offset(center.x, center.y - radius + 6f)
                    val baseLeft = Offset(center.x - 14f, center.y - radius + 34f)
                    val baseRight = Offset(center.x + 14f, center.y - radius + 34f)
                    val north = Path().apply {
                        moveTo(tip.x, tip.y); lineTo(baseLeft.x, baseLeft.y); lineTo(baseRight.x, baseRight.y); close()
                    }
                    drawPath(north, color = northColor)
                }

                // Fixed index triangle at the very top, pointing into the dial (the way you face).
                val idx = Path().apply {
                    moveTo(center.x, center.y - radius + 18f)
                    lineTo(center.x - 10f, center.y - radius - 8f)
                    lineTo(center.x + 10f, center.y - radius - 8f)
                    close()
                }
                drawPath(idx, color = indexColor)

                drawCircle(color = hubColor, radius = 8f, center = center)
            }
        }
    }
}
