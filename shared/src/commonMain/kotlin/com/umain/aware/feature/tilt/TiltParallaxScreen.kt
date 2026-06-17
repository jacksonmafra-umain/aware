package com.umain.aware.feature.tilt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.aware.core.CollectSensors
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.Reading
import com.umain.aware.core.SensorSource
import com.umain.aware.core.SensorType
import com.umain.aware.core.SensorUpdate
import androidx.compose.material3.ElevatedCard
import org.koin.compose.koinInject

/**
 * Tilt parallax: angular velocity from the gyroscope is integrated into a small rotation (with a
 * decay so the card eases back to flat when the phone is still) and applied to a hero card — the
 * subtle 3D tilt you see on app store cards.
 */
@Composable
fun TiltParallaxScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val types = remember { listOf(SensorType.GYROSCOPE) }

    var rotX by remember { mutableStateOf(0f) }
    var rotY by remember { mutableStateOf(0f) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                val r = update.reading
                if (r is Reading.Gyroscope) {
                    // integrate (small step) then decay toward flat; clamp to a gentle range
                    rotX = ((rotX + r.x * 2f) * 0.9f).coerceIn(-12f, 12f)
                    rotY = ((rotY + r.y * 2f) * 0.9f).coerceIn(-12f, 12f)
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    FeatureScaffold("Tilt parallax", platform, onBack) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .graphicsLayer {
                    rotationX = rotX
                    rotationY = rotY
                    cameraDistance = 12f * density
                }
                .shadow(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Tilt your phone — the card leans with it and settles back when you hold still.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
