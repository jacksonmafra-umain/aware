package com.umain.aware.feature.pad

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import com.umain.aware.core.FeatureScaffold
import org.koin.compose.koinInject
import com.umain.aware.core.SensorSource

/**
 * Signature pad: builds strokes from finger drags and renders them.
 *
 * Note: this would ideally consume KSensor's TOUCH_GESTURES, but KSensor 3.80.0 never initialises
 * its Android touch monitor (its startup initializer doesn't call the internal
 * `TouchGesturesMonitor.init()`, and registering an observer doesn't hook the current Activity), so
 * the library emits no touch events. We therefore capture touch directly with Compose's pointer
 * input — the one screen where the KSensor signal isn't usable on this version.
 */
@Composable
fun SignaturePadScreen(onBack: () -> Unit) {
    // Injected to keep the screen consistent with the others (and ready to switch back to
    // SensorType.TOUCH_GESTURES once the library initialises its monitor).
    @Suppress("UNUSED_VARIABLE")
    val source = koinInject<SensorSource>()

    var strokes by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    val ink = MaterialTheme.colorScheme.primary

    FeatureScaffold("Signature pad", platform = null, onBack = onBack) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { start -> strokes = strokes + listOf(listOf(start)) },
                            onDrag = { change, _ ->
                                change.consume()
                                if (strokes.isNotEmpty()) {
                                    val updated = strokes.last() + change.position
                                    strokes = strokes.dropLast(1) + listOf(updated)
                                }
                            },
                        )
                    },
            ) {
                for (stroke in strokes) {
                    for (i in 1 until stroke.size) {
                        drawLine(color = ink, start = stroke[i - 1], end = stroke[i], strokeWidth = 6f)
                    }
                }
            }
        }
        OutlinedButton(onClick = { strokes = emptyList() }) {
            Text("Clear")
        }
        Text(
            "Sign with your finger.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
