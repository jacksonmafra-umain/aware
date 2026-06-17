package com.umain.aware.feature.privacy

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
import org.koin.compose.koinInject

/**
 * Privacy shield: hides a bank balance whenever the app is backgrounded, the device is locked, or
 * the screen is off — combining APP_VISIBILITY, LOCK and SCREEN_STATE via [ShieldPolicy].
 */
@Composable
fun PrivacyShieldScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.APP_VISIBILITY, StateType.LOCK, StateType.SCREEN_STATE) }

    // Default to the safe (visible) state until the platform reports otherwise.
    var appVisible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var screenOn by remember { mutableStateOf(true) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            when (val r = update.reading) {
                is StateReading.AppVisibility -> appVisible = r.isVisible
                is StateReading.Lock -> locked = r.isLocked
                is StateReading.ScreenStatus -> screenOn = r.isOn
                else -> Unit
            }
        }
    }

    val hidden = ShieldPolicy.hidden(appVisible, locked, screenOn)

    FeatureScaffold("Privacy shield", platform, onBack) {
        MetricCard {
            MetricText(if (hidden) "••••••" else "$12,345.67", "account balance")
            Text(
                if (hidden) {
                    "Hidden — the app is backgrounded, locked, or the screen is off."
                } else {
                    "Visible — app is in the foreground, unlocked, screen on."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
