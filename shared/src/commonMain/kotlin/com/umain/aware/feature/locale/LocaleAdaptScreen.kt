package com.umain.aware.feature.locale

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
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
 * Locale adapt: formats a sample price with the device country's currency symbol via [RegionFormat]
 * and mirrors the layout direction reported by the LOCALE state's `isRtl`.
 */
@Composable
fun LocaleAdaptScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.LOCALE) }

    var locale by remember { mutableStateOf<StateReading.Locale?>(null) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            val r = update.reading
            if (r is StateReading.Locale) locale = r
        }
    }

    val l = locale
    val price = RegionFormat.format(1299.0, l?.countryCode ?: "")
    val direction = if (l?.isRtl == true) LayoutDirection.Rtl else LayoutDirection.Ltr

    FeatureScaffold("Locale adapt", platform, onBack) {
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
            MetricCard {
                MetricText(price, "formatted for ${l?.countryCode ?: "—"}")
                StatusPill(if (l?.isRtl == true) "Right-to-left" else "Left-to-right", active = l?.isRtl == true)
                Text(
                    l?.displayName?.let { "Device locale: $it" } ?: "Waiting for locale…",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
