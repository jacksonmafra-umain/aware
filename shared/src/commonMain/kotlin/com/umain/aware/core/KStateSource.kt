package com.umain.aware.core

// ─── CENTRALISED KSENSOR IMPORT (see KSensorSource.kt for the rationale) ─────────────────────
import org.kmp.ksensor.state.KState
import org.kmp.ksensor.state.StateData
import org.kmp.ksensor.state.StateType as KStateType
import org.kmp.ksensor.state.StateUpdate as KStateUpdate
// ─────────────────────────────────────────────────────────────────────────────────────────────
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

private const val TAG = "KStateSource"

/**
 * The real [StateSource]: wraps the global `KState` object.
 *
 * Lifecycle contract: each type is observed by a cold flow that adds its observer on first
 * collection and removes it in [onCompletion] (so nothing leaks).
 *
 * Why one observer per type: KSensor's Android `addObserver` calls `awaitClose` *inside* its
 * per-type loop, so subscribing to several types in a single call invokes `awaitClose` more than
 * once and crashes the `callbackFlow`. Observing each type in its own subscription and merging the
 * results sidesteps that. CONNECTIVITY and ACTIVE_NETWORK are served by one underlying observer
 * that already emits both, so that pair is collapsed to avoid a duplicate registration.
 */
class KStateSource : StateSource {

    override fun states(types: List<StateType>): Flow<StateUpdate> {
        val observed = types.distinct().toMutableList()
        if (StateType.CONNECTIVITY in observed && StateType.ACTIVE_NETWORK in observed) {
            observed.remove(StateType.ACTIVE_NETWORK) // the CONNECTIVITY observer emits both
        }
        val flows = observed.map { observe(it) }
        return if (flows.size == 1) flows.first() else flows.merge()
    }

    private fun observe(type: StateType): Flow<StateUpdate> {
        val ksTypes = listOf(type.toKState())
        return KState.addObserver(ksTypes)
            .map { it.toDomain() }
            .onStart { AwareLog.d(TAG, "addObserver($type)") }
            .catch { cause ->
                // e.g. ACCESS_NETWORK_STATE / Bluetooth permission missing — surface as a handled
                // error instead of crashing the collecting screen.
                AwareLog.e(TAG, "state stream failed for $type", cause)
                emit(StateUpdate.Error(cause.message ?: cause.toString()))
            }
            .onCompletion { cause ->
                AwareLog.d(TAG, "removeObserver($type)" + (cause?.let { " after $it" } ?: ""))
                KState.removeObserver(ksTypes)
            }
    }
}

private fun StateType.toKState(): KStateType = when (this) {
    StateType.APP_VISIBILITY -> KStateType.APP_VISIBILITY
    StateType.CONNECTIVITY -> KStateType.CONNECTIVITY
    StateType.ACTIVE_NETWORK -> KStateType.ACTIVE_NETWORK
    StateType.LOCATION -> KStateType.LOCATION
    StateType.SCREEN_STATE -> KStateType.SCREEN
    StateType.VOLUME -> KStateType.VOLUME
    StateType.LOCALE -> KStateType.LOCALE
    StateType.BATTERY -> KStateType.BATTERY
    StateType.LOCK -> KStateType.LOCK
    StateType.BLE_CONNECTIONS -> KStateType.BLE_CONNECTIONS
    StateType.BLE_DISCOVERS -> KStateType.BLE_DISCOVERS
}

private fun KStateUpdate.toDomain(): StateUpdate = when (this) {
    is KStateUpdate.Data -> {
        // StateData is sealed, so this maps exhaustively onto Aware's own StateReading type.
        val reading: StateReading = when (val d = data) {
            is StateData.AppVisibilityStatus -> StateReading.AppVisibility(d.isAppVisible)
            is StateData.ConnectivityStatus -> StateReading.Connectivity(d.isConnected)
            is StateData.CurrentActiveNetwork -> StateReading.ActiveNetwork(d.activeNetwork.name)
            is StateData.LocationStatus -> StateReading.LocationStatus(d.isLocationOn)
            is StateData.ScreenStatus -> StateReading.ScreenStatus(d.isScreenOn)
            is StateData.VolumeStatus -> StateReading.Volume(d.volumePercentage)
            is StateData.LocaleStatus -> StateReading.Locale(
                languageCode = d.languageCode,
                countryCode = d.countryCode,
                fullLocale = d.fullLocaleString,
                displayName = d.displayName,
                isRtl = d.isRTL,
            )
            is StateData.BatteryStatus -> StateReading.Battery(
                levelPercent = d.levelPercent,
                charging = d.chargingState.toDomain(),
                health = d.health?.toDomain(),
                temperatureC = d.temperatureC,
            )
            is StateData.LockStatus -> StateReading.Lock(d.isDeviceLocked)
            is StateData.BleConnectionStatus ->
                StateReading.BleConnected(d.connectedDevices.map { StateReading.BleDevice(it.id, it.name) })
            is StateData.BleDiscoversStatus ->
                StateReading.BleDiscovered(d.discoveredDevices.map { StateReading.BleDevice(it.id, it.name) })
        }
        StateUpdate.Data(reading, platformType.name)
    }
    is KStateUpdate.Error -> {
        AwareLog.e(TAG, "KState reported an error", exception)
        StateUpdate.Error(exception.message ?: exception.toString())
    }
}

private fun StateData.BatteryStatus.ChargingState.toDomain(): ChargingState = when (this) {
    StateData.BatteryStatus.ChargingState.UNKNOWN -> ChargingState.UNKNOWN
    StateData.BatteryStatus.ChargingState.DISCHARGING -> ChargingState.DISCHARGING
    StateData.BatteryStatus.ChargingState.CHARGING -> ChargingState.CHARGING
    StateData.BatteryStatus.ChargingState.FULL -> ChargingState.FULL
}

private fun StateData.BatteryStatus.BatteryHealth.toDomain(): BatteryHealth = when (this) {
    StateData.BatteryStatus.BatteryHealth.UNKNOWN -> BatteryHealth.UNKNOWN
    StateData.BatteryStatus.BatteryHealth.GOOD -> BatteryHealth.GOOD
    StateData.BatteryStatus.BatteryHealth.OVERHEAT -> BatteryHealth.OVERHEAT
    StateData.BatteryStatus.BatteryHealth.DEAD -> BatteryHealth.DEAD
    StateData.BatteryStatus.BatteryHealth.OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
    StateData.BatteryStatus.BatteryHealth.UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
    StateData.BatteryStatus.BatteryHealth.COLD -> BatteryHealth.COLD
}
