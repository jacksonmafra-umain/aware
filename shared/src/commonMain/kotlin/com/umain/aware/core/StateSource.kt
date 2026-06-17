package com.umain.aware.core

import kotlinx.coroutines.flow.Flow

/** Device/app states Aware knows how to consume. Aware-owned mirror of KSensor's `StateType`. */
enum class StateType {
    APP_VISIBILITY,
    CONNECTIVITY,
    ACTIVE_NETWORK,
    LOCATION,
    SCREEN_STATE,
    VOLUME,
    LOCALE,
    BATTERY,
    LOCK,
    BLE_CONNECTIONS,
    BLE_DISCOVERS,
}

/** Charging state, mirroring KSensor's `BatteryStatus.ChargingState`. */
enum class ChargingState { UNKNOWN, DISCHARGING, CHARGING, FULL }

/** Battery health, mirroring KSensor's `BatteryStatus.BatteryHealth`. */
enum class BatteryHealth { UNKNOWN, GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, UNSPECIFIED_FAILURE, COLD }

/** A single state reading in Aware's own vocabulary. */
sealed interface StateReading {
    data class AppVisibility(val isVisible: Boolean) : StateReading
    data class Connectivity(val isConnected: Boolean) : StateReading
    data class ActiveNetwork(val network: String) : StateReading
    data class LocationStatus(val isOn: Boolean) : StateReading
    data class ScreenStatus(val isOn: Boolean) : StateReading
    data class Volume(val percent: Int) : StateReading
    data class Locale(
        val languageCode: String,
        val countryCode: String,
        val fullLocale: String,
        val displayName: String,
        val isRtl: Boolean,
    ) : StateReading

    data class Battery(
        val levelPercent: Int?,
        val charging: ChargingState,
        val health: BatteryHealth?,
        val temperatureC: Float?,
    ) : StateReading

    data class Lock(val isLocked: Boolean) : StateReading

    /** A discovered/connected Bluetooth device. */
    data class BleDevice(val id: String, val name: String)

    data class BleConnected(val devices: List<BleDevice>) : StateReading
    data class BleDiscovered(val devices: List<BleDevice>) : StateReading
}

/** A state emission: either a mapped [StateReading] or a generic error message. */
sealed interface StateUpdate {
    data class Data(val reading: StateReading, val platform: String) : StateUpdate

    /** Generic error message; see [SensorUpdate.Error] for why the shape is opaque. */
    data class Error(val message: String) : StateUpdate
}

/**
 * Abstraction over a live source of device/app states (DIP). Real binding is [KStateSource]; tests
 * substitute `FakeStateSource`. Kept separate from [SensorSource] (ISP).
 */
interface StateSource {
    /**
     * Returns a cold [Flow] that adds observers for [types] on first collection and is required to
     * remove them when collection completes or is cancelled.
     */
    fun states(types: List<StateType>): Flow<StateUpdate>
}
