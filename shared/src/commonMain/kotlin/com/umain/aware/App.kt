package com.umain.aware

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umain.aware.feature.battery.BatterySaverScreen
import com.umain.aware.feature.ble.NearbyDevicesScreen
import com.umain.aware.feature.compass.CompassScreen
import com.umain.aware.feature.floors.FloorClimbScreen
import com.umain.aware.feature.light.AutoDarkModeScreen
import com.umain.aware.feature.locale.LocaleAdaptScreen
import com.umain.aware.feature.network.SmartDownloadScreen
import com.umain.aware.feature.pad.SignaturePadScreen
import com.umain.aware.feature.pocket.PocketModeScreen
import com.umain.aware.feature.privacy.PrivacyShieldScreen
import com.umain.aware.feature.rotate.AutoRotateScreen
import com.umain.aware.feature.shake.ShakeToReportScreen
import com.umain.aware.feature.steps.StepGoalScreen
import com.umain.aware.feature.tilt.TiltParallaxScreen
import com.umain.aware.feature.trip.TripTrackerScreen
import com.umain.aware.feature.volume.VolumeHudScreen
import org.koin.compose.KoinContext

/**
 * One entry in the Aware gallery. A feature is fully described by its metadata plus a [screen]
 * factory that takes an `onBack` callback. Adding a feature means adding its `Feature` to
 * [awareFeatures] — existing features are never edited (OCP).
 */
data class Feature(
    val id: String,
    val title: String,
    val tagline: String,
    val signals: String,
    val screen: @Composable (onBack: () -> Unit) -> Unit,
)

/**
 * The single registry of demos. Each feature appends exactly one entry here; the list's order is
 * the gallery's order.
 */
fun awareFeatures(): List<Feature> = listOf(
    Feature(
        id = "shake",
        title = "Shake to report",
        tagline = "Shake the phone to file a bug report.",
        signals = "ACCELEROMETER",
        screen = { onBack -> ShakeToReportScreen(onBack) },
    ),
    Feature(
        id = "steps",
        title = "Daily step goal",
        tagline = "Track steps since you opened the app against a goal.",
        signals = "STEP_COUNTER + STEP_DETECTOR",
        screen = { onBack -> StepGoalScreen(onBack) },
    ),
    Feature(
        id = "floors",
        title = "Floors climbed",
        tagline = "Counts floors from air-pressure changes.",
        signals = "BAROMETER",
        screen = { onBack -> FloorClimbScreen(onBack) },
    ),
    Feature(
        id = "compass",
        title = "Compass",
        tagline = "A needle that points to magnetic north.",
        signals = "MAGNETOMETER",
        screen = { onBack -> CompassScreen(onBack) },
    ),
    Feature(
        id = "trip",
        title = "Trip tracker",
        tagline = "Adds up distance travelled between GPS fixes.",
        signals = "LOCATION sensor + state",
        screen = { onBack -> TripTrackerScreen(onBack) },
    ),
    Feature(
        id = "light",
        title = "Auto dark mode",
        tagline = "Dims the page when the room gets dark.",
        signals = "LIGHT",
        screen = { onBack -> AutoDarkModeScreen(onBack) },
    ),
    Feature(
        id = "network",
        title = "Smart download",
        tagline = "HD on Wi-Fi, SD on cellular, paused offline.",
        signals = "CONNECTIVITY + ACTIVE_NETWORK",
        screen = { onBack -> SmartDownloadScreen(onBack) },
    ),
    Feature(
        id = "battery",
        title = "Battery saver",
        tagline = "Saver below 20%, sync while charging, overheat warning.",
        signals = "BATTERY",
        screen = { onBack -> BatterySaverScreen(onBack) },
    ),
    Feature(
        id = "privacy",
        title = "Privacy shield",
        tagline = "Hides your balance when the app isn't front-and-centre.",
        signals = "APP_VISIBILITY + LOCK + SCREEN_STATE",
        screen = { onBack -> PrivacyShieldScreen(onBack) },
    ),
    Feature(
        id = "locale",
        title = "Locale adapt",
        tagline = "Currency symbol and text direction follow the device region.",
        signals = "LOCALE",
        screen = { onBack -> LocaleAdaptScreen(onBack) },
    ),
    Feature(
        id = "tilt",
        title = "Tilt parallax",
        tagline = "A hero card that leans as you tilt the phone.",
        signals = "GYROSCOPE",
        screen = { onBack -> TiltParallaxScreen(onBack) },
    ),
    Feature(
        id = "rotate",
        title = "Auto-rotate video",
        tagline = "Goes fullscreen in landscape, inline in portrait.",
        signals = "DEVICE_ORIENTATION",
        screen = { onBack -> AutoRotateScreen(onBack) },
    ),
    Feature(
        id = "pocket",
        title = "Pocket mode",
        tagline = "Pauses playback when something covers the sensor.",
        signals = "PROXIMITY",
        screen = { onBack -> PocketModeScreen(onBack) },
    ),
    Feature(
        id = "pad",
        title = "Signature pad",
        tagline = "Draws your strokes from the touch-gesture stream.",
        signals = "TOUCH_GESTURES",
        screen = { onBack -> SignaturePadScreen(onBack) },
    ),
    Feature(
        id = "volume",
        title = "Volume HUD",
        tagline = "A custom volume bar with a 'too loud' warning.",
        signals = "VOLUME",
        screen = { onBack -> VolumeHudScreen(onBack) },
    ),
    Feature(
        id = "ble",
        title = "Nearby devices",
        tagline = "Connected vs in-range Bluetooth devices.",
        signals = "BLE_CONNECTIONS + BLE_DISCOVERS",
        screen = { onBack -> NearbyDevicesScreen(onBack) },
    ),
    // feature entries are registered here, one per feature.
)

/**
 * App root: runs inside a [KoinContext] so screens can `koinInject()` the source abstractions,
 * then drives a trivial one-level back stack — the gallery, or the currently open feature.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            var selected by remember { mutableStateOf<Feature?>(null) }
            val features = remember { awareFeatures() }

            when (val current = selected) {
                null -> Gallery(features = features, onOpen = { selected = it })
                else -> current.screen { selected = null }
            }
        }
    }
}

@Composable
private fun Gallery(features: List<Feature>, onOpen: (Feature) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("Aware", style = MaterialTheme.typography.displaySmall)
                    Text(
                        "Everyday product features, each powered by a device sensor or state via KSensor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(features, key = { it.id }) { feature ->
                FeatureCard(feature = feature, onClick = { onOpen(feature) })
            }
            if (features.isEmpty()) {
                item {
                    Text(
                        "No demos registered yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: Feature, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(feature.title, style = MaterialTheme.typography.titleMedium)
            Text(
                feature.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                feature.signals,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
