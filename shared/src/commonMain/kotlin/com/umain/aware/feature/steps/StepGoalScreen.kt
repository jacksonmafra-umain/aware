package com.umain.aware.feature.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.umain.aware.core.CollectSensors
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.MetricText
import com.umain.aware.core.Reading
import com.umain.aware.core.SensorSource
import com.umain.aware.core.SensorType
import com.umain.aware.core.SensorUpdate
import com.umain.aware.core.StatusPill
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

/**
 * Daily step goal. The cumulative STEP_COUNTER drives a [StepSession] (baseline + progress); the
 * STEP_DETECTOR only lights a transient "walking now" indicator.
 */
@Composable
fun StepGoalScreen(onBack: () -> Unit) {
    val source = koinInject<SensorSource>()
    val session = remember { StepSession() }
    val types = remember { listOf(SensorType.STEP_COUNTER, SensorType.STEP_DETECTOR) }

    var steps by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var reached by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var walking by remember { mutableStateOf(false) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectSensors(source, types) { update ->
        when (update) {
            is SensorUpdate.Data -> {
                platform = update.platform
                when (val r = update.reading) {
                    is Reading.StepCount -> {
                        session.onCounter(r.steps)
                        steps = session.stepsSinceStart
                        progress = session.progress
                        reached = session.goalReached
                    }
                    Reading.StepTick -> tick++
                    else -> Unit
                }
            }
            is SensorUpdate.Error -> Unit
        }
    }

    // Keep "walking" lit until 1.2s after the most recent step event.
    LaunchedEffect(tick) {
        if (tick > 0) {
            walking = true
            delay(1200)
            walking = false
        }
    }

    FeatureScaffold("Daily step goal", platform, onBack) {
        MetricCard {
            MetricText("$steps", "of ${session.goal} steps")
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            StatusPill(if (walking) "Walking now" else "Idle", active = walking)
            if (reached) {
                Text(
                    "Goal reached — nice work!",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
