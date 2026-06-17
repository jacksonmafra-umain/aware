package com.umain.aware.feature.ble

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.aware.core.CollectStates
import com.umain.aware.core.FeatureScaffold
import com.umain.aware.core.MetricCard
import com.umain.aware.core.StateReading
import com.umain.aware.core.StateSource
import com.umain.aware.core.StateType
import com.umain.aware.core.StateUpdate
import org.koin.compose.koinInject

/**
 * Nearby devices: a "find my headphones" screen that lists which Bluetooth devices are connected
 * versus merely discovered in range, from the BLE_CONNECTIONS and BLE_DISCOVERS states.
 */
@Composable
fun NearbyDevicesScreen(onBack: () -> Unit) {
    val source = koinInject<StateSource>()
    val types = remember { listOf(StateType.BLE_CONNECTIONS, StateType.BLE_DISCOVERS) }

    var connected by remember { mutableStateOf<List<StateReading.BleDevice>>(emptyList()) }
    var discovered by remember { mutableStateOf<List<StateReading.BleDevice>>(emptyList()) }
    var platform by remember { mutableStateOf<String?>(null) }

    CollectStates(source, types) { update ->
        if (update is StateUpdate.Data) {
            platform = update.platform
            when (val r = update.reading) {
                is StateReading.BleConnected -> connected = r.devices
                is StateReading.BleDiscovered -> discovered = r.devices
                else -> Unit
            }
        }
    }

    // Devices in range but not currently connected.
    val connectedIds = connected.map { it.id }.toSet()
    val inRangeOnly = discovered.filter { it.id !in connectedIds }

    FeatureScaffold("Nearby devices", platform, onBack) {
        MetricCard {
            DeviceSection("Connected", connected, "No connected devices.")
        }
        MetricCard {
            DeviceSection("In range", inRangeOnly, "Scanning for nearby devices…")
        }
    }
}

@Composable
private fun DeviceSection(title: String, devices: List<StateReading.BleDevice>, empty: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (devices.isEmpty()) {
            Text(
                empty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            )
        } else {
            devices.forEach { device ->
                Text(
                    device.name.ifBlank { device.id },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
