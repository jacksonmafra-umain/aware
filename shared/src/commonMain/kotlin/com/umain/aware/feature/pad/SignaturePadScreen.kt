package com.umain.aware.feature.pad

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.aware.core.CollectSensors
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.Reading
import com.umain.aware.core.SensorSource
import com.umain.aware.core.SensorType
import com.umain.aware.core.SensorUpdate
import org.koin.compose.koinInject

/**
 * Signature pad: assembles strokes from the TOUCH_GESTURES stream. The gesture phase is matched by
 * string ("down"/"up"/anything-else == move) so it compiles whatever the library's enum is called.
 * Points are auto-fit to the canvas, so the drawing is visible regardless of the coordinate space
 * KSensor reports.
 */
@Composable
fun SignaturePadScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val types = remember { listOf(SensorType.TOUCH_GESTURES) }

    var strokes by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var drawing by remember { mutableStateOf(false) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Touch) {
                    val phase = r.type.lowercase()
                    val p = Offset(r.x, r.y)
                    when {
                        phase.contains("down") || phase.contains("start") || phase.contains("begin") -> {
                            drawing = true
                            strokes = strokes + listOf(listOf(p))
                        }
                        phase.contains("up") || phase.contains("end") || phase.contains("cancel") || phase.contains("release") -> {
                            drawing = false
                        }
                        else -> { // move
                            if (drawing && strokes.isNotEmpty()) {
                                strokes = strokes.dropLast(1) + listOf(strokes.last() + p)
                            } else {
                                drawing = true
                                strokes = strokes + listOf(listOf(p))
                            }
                        }
                    }
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    val ink = MaterialTheme.colorScheme.primary

    FeatureScaffold("Signature pad", platform, onBack) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Canvas(modifier = Modifier.fillMaxWidth()) {
                val all = strokes.flatten()
                if (all.isEmpty()) return@Canvas
                val minX = all.minOf { it.x }
                val maxX = all.maxOf { it.x }
                val minY = all.minOf { it.y }
                val maxY = all.maxOf { it.y }
                val pad = 24f
                val spanX = (maxX - minX).coerceAtLeast(1f)
                val spanY = (maxY - minY).coerceAtLeast(1f)

                fun map(o: Offset) = Offset(
                    x = pad + (o.x - minX) / spanX * (size.width - 2 * pad),
                    y = pad + (o.y - minY) / spanY * (size.height - 2 * pad),
                )

                for (stroke in strokes) {
                    for (i in 1 until stroke.size) {
                        drawLine(color = ink, start = map(stroke[i - 1]), end = map(stroke[i]), strokeWidth = 6f)
                    }
                }
            }
        }
        OutlinedButton(onClick = { strokes = emptyList(); drawing = false }) {
            Text("Clear")
        }
        Text(
            "Sign with your finger. The pad rebuilds your strokes from the touch-gesture stream.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Unspecified,
            textAlign = TextAlign.Center,
        )
    }
}
