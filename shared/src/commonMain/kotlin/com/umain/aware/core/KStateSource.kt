package com.umain.aware.core

// ─── CENTRALISED KSENSOR IMPORT (see KSensorSource.kt for the rationale) ─────────────────────
import io.github.shadadman.ksensor.KState
import io.github.shadadman.ksensor.StateType as KStateType
import io.github.shadadman.ksensor.StateUpdate as KStateUpdate
import io.github.shadadman.ksensor.AppVisibilityStatus
import io.github.shadadman.ksensor.LocationStatus
import io.github.shadadman.ksensor.ScreenStatus
import io.github.shadadman.ksensor.LockStatus
import io.github.shadadman.ksensor.CurrentActiveNetwork
import io.github.shadadman.ksensor.ConnectivityStatus
import io.github.shadadman.ksensor.VolumeStatus
import io.github.shadadman.ksensor.LocaleInfo
import io.github.shadadman.ksensor.BatteryStatus
import io.github.shadadman.ksensor.BleConnectionStatus
import io.github.shadadman.ksensor.BleDiscoversStatus
// ─────────────────────────────────────────────────────────────────────────────────────────────
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

/**
 * The real [StateSource]: wraps the global `KState` object.
 *
 * Lifecycle contract: `KState.addObserver` observes on first collection; [onCompletion] guarantees
 * `removeObserver` runs on completion/cancellation, so no observer is ever leaked.
 */
class KStateSource : StateSource {

    override fun states(types: List<StateType>): Flow<StateUpdate> {
        val ksTypes = types.map { it.toKState() }
        return KState.addObserver(ksTypes)
            .map { it.toDomain() }
            .onCompletion { KState.removeObserver(ksTypes) }
    }
}

private fun StateType.toKState(): KStateType = when (this) {
    StateType.APP_VISIBILITY -> KStateType.APP_VISIBILITY
    StateType.CONNECTIVITY -> KStateType.CONNECTIVITY
    StateType.ACTIVE_NETWORK -> KStateType.ACTIVE_NETWORK
    StateType.LOCATION -> KStateType.LOCATION
    StateType.SCREEN_STATE -> KStateType.SCREEN_STATE
    StateType.VOLUME -> KStateType.VOLUME
    StateType.LOCALE -> KStateType.LOCALE
    StateType.BATTERY -> KStateType.BATTERY
    StateType.LOCK -> KStateType.LOCK
    StateType.BLE_CONNECTIONS -> KStateType.BLE_CONNECTIONS
    StateType.BLE_DISCOVERS -> KStateType.BLE_DISCOVERS
}

private fun KStateUpdate.toDomain(): StateUpdate = when (this) {
    is KStateUpdate.Data -> {
        val reading: StateReading? = when (val d = data) {
            is AppVisibilityStatus -> StateReading.AppVisibility(d.isAppVisible)
            is ConnectivityStatus -> StateReading.Connectivity(d.isConnected)
            is CurrentActiveNetwork -> StateReading.ActiveNetwork(d.activeNetwork.toString())
            is LocationStatus -> StateReading.LocationStatus(d.isLocationOn)
            is ScreenStatus -> StateReading.ScreenStatus(d.isScreenOn)
            is VolumeStatus -> StateReading.Volume(d.volumePercentage)
            is LocaleInfo -> StateReading.Locale(
                languageCode = d.languageCode,
                countryCode = d.countryCode,
                fullLocale = d.fullLocaleString,
                displayName = d.displayName,
                isRtl = d.isRTL,
            )
            is BatteryStatus -> StateReading.Battery(
                levelPercent = d.levelPercent,
                charging = d.chargingState.toDomain(),
                health = d.health?.toDomain(),
                temperatureC = d.temperatureC,
            )
            is LockStatus -> StateReading.Lock(d.isDeviceLocked)
            is BleConnectionStatus -> StateReading.BleConnected(
                d.connectedDevices.map { StateReading.BleDevice(it.id, it.name) }
            )
            is BleDiscoversStatus -> StateReading.BleDiscovered(
                d.discoveredDevices.map { StateReading.BleDevice(it.id, it.name) }
            )
            else -> null
        }
        if (reading != null) StateUpdate.Data(reading, platformType.toString())
        else StateUpdate.Error("Unmapped KState reading: $data")
    }
    is KStateUpdate.Error -> StateUpdate.Error(this.toString())
}

private fun BatteryStatus.ChargingState.toDomain(): ChargingState = when (this) {
    BatteryStatus.ChargingState.UNKNOWN -> ChargingState.UNKNOWN
    BatteryStatus.ChargingState.DISCHARGING -> ChargingState.DISCHARGING
    BatteryStatus.ChargingState.CHARGING -> ChargingState.CHARGING
    BatteryStatus.ChargingState.FULL -> ChargingState.FULL
}

private fun BatteryStatus.BatteryHealth.toDomain(): BatteryHealth = when (this) {
    BatteryStatus.BatteryHealth.UNKNOWN -> BatteryHealth.UNKNOWN
    BatteryStatus.BatteryHealth.GOOD -> BatteryHealth.GOOD
    BatteryStatus.BatteryHealth.OVERHEAT -> BatteryHealth.OVERHEAT
    BatteryStatus.BatteryHealth.DEAD -> BatteryHealth.DEAD
    BatteryStatus.BatteryHealth.OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
    BatteryStatus.BatteryHealth.UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
    BatteryStatus.BatteryHealth.COLD -> BatteryHealth.COLD
}
