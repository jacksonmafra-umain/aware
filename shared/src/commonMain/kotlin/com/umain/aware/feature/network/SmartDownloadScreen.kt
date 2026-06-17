package com.umain.aware.feature.network

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.umain.aware.core.CollectStates
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.MetricText
import com.umain.aware.core.StateReading
import com.umain.aware.core.StateSource
import com.umain.aware.core.StateType
import com.umain.aware.core.StateUpdate
import com.umain.aware.core.StatusPill
import org.koin.compose.koinInject

/**
 * Smart download: combines CONNECTIVITY and ACTIVE_NETWORK states and lets [DownloadPolicy] choose
 * paused / SD / HD — the classic "only download HD on Wi-Fi" behaviour.
 */
@Composable
fun SmartDownloadScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.CONNECTIVITY, StateType.ACTIVE_NETWORK) }

    var connected by remember { mutableStateOf(false) }
    var network by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            when (val r = update.reading) {
                is StateReading.Connectivity -> connected = r.isConnected
                is StateReading.ActiveNetwork -> network = r.network
                else -> Unit
            }
        }
    }

    val quality = DownloadPolicy.decide(connected, network)
    val caption = when (quality) {
        DownloadQuality.PAUSED -> "Offline — downloads paused"
        DownloadQuality.SD -> "On cellular — downloading in SD to save data"
        DownloadQuality.HD -> "On Wi-Fi — downloading in HD"
    }

    FeatureScaffold("Smart download", platform, onBack) {
        MetricCard {
            MetricText(quality.name, "download quality")
            StatusPill(if (connected) "Online" else "Offline", active = connected)
            Text(
                caption,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
