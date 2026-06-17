package com.umain.aware.feature.tilt

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.aware.core.CollectSensors
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.Reading
import com.umain.aware.core.SensorSource
import com.umain.aware.core.SensorType
import com.umain.aware.core.SensorUpdate
import org.koin.compose.koinInject
import kotlin.math.sqrt
import kotlin.time.TimeSource

/**
 * Tilt marble: a marble rolls around inside the card as you tilt the phone, simulated by
 * [MarbleBox]. We feed it the *direction* of gravity (each axis over the total magnitude), which is
 * the sine of the tilt and runs 0→1 across the full tilt — unit-independent, so it behaves the same
 * regardless of how KSensor normalises the raw accelerometer. Screen-space mapping is (-x, y);
 * device +x is screen-right and +y is screen-up, so flip a sign here if it ever rolls the wrong way.
 */
@Composable
fun MarbleScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val marble = remember { MarbleBox() }
    val clock = remember { TimeSource.Monotonic.markNow() }
    val types = remember { listOf(SensorType.ACCELEROMETER) }

    var lastMs by remember { mutableStateOf(0L) }
    var ballX by remember { mutableStateOf(0.5f) }
    var ballY by remember { mutableStateOf(0.5f) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        if (update is SensorUpdate.Data) {
            platform = update.platform
            val r = update.reading
            if (r is Reading.Accelerometer) {
                val magnitude = sqrt(r.x * r.x + r.y * r.y + r.z * r.z)
                if (magnitude > 1e-4f) {
                    val now = clock.elapsedNow().inWholeMilliseconds
                    val dt = if (lastMs == 0L) 0.016f else (now - lastMs) / 1000f
                    lastMs = now
                    marble.onTilt(tiltX = -r.x / magnitude, tiltY = r.y / magnitude, dtSeconds = dt)
                    ballX = marble.x
                    ballY = marble.y
                }
            }
        }
    }

    val ballColor = MaterialTheme.colorScheme.primary
    val wallColor = MaterialTheme.colorScheme.outline

    FeatureScaffold("Tilt marble", platform, onBack) {
        MetricCard {
            Text(
                "Tilt the phone — the marble rolls downhill and bounces off the walls.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val pad = 6f
                val radius = 24f
                drawRoundRect(
                    color = wallColor,
                    cornerRadius = CornerRadius(20f, 20f),
                    style = Stroke(width = 4f),
                )
                val left = pad + radius
                val top = pad + radius
                val spanX = (size.width - 2 * left).coerceAtLeast(1f)
                val spanY = (size.height - 2 * top).coerceAtLeast(1f)
                drawCircle(
                    color = ballColor,
                    radius = radius,
                    center = Offset(left + ballX * spanX, top + ballY * spanY),
                )
            }
        }
    }
}
