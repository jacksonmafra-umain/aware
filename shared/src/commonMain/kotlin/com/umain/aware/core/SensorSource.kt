package com.umain.aware.core

import kotlinx.coroutines.flow.Flow

/**
 * The set of sensors Aware knows how to consume. This mirrors KSensor's `SensorType`, but is an
 * Aware-owned type on purpose: nothing outside [KSensorSource] is allowed to touch KSensor types
 * (DIP). Derivers and screens speak only this vocabulary.
 */
enum class SensorType {
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    BAROMETER,
    STEP_COUNTER,
    STEP_DETECTOR,
    LOCATION,
    DEVICE_ORIENTATION,
    PROXIMITY,
    LIGHT,
    TOUCH_GESTURES,
}

/**
 * A single, fully-typed sensor reading in Aware's own vocabulary. [KSensorSource] maps the
 * library's data models onto these so the rest of the app never imports a KSensor class.
 */
sealed interface Reading {
    data class Accelerometer(val x: Float, val y: Float, val z: Float) : Reading
    data class Gyroscope(val x: Float, val y: Float, val z: Float) : Reading
    data class Magnetometer(val x: Float, val y: Float, val z: Float) : Reading

    /** Barometric pressure in hPa. */
    data class Barometer(val pressure: Float) : Reading

    /** Cumulative step count reported by the hardware step counter. */
    data class StepCount(val steps: Int) : Reading

    /** A single step event from the step detector (carries no payload). */
    data object StepTick : Reading

    data class Location(val lat: Double?, val lon: Double?, val alt: Double?) : Reading

    /** Device orientation; [name] is the library enum's name, [raw] its integer code. */
    data class Orientation(val name: String, val raw: Int) : Reading

    data class Proximity(val distanceCm: Float, val isNear: Boolean) : Reading

    /** Ambient light in lux. */
    data class Light(val lux: Float) : Reading

    /** Touch gesture sample; [type] is matched defensively by string (see SignaturePadScreen). */
    data class Touch(val x: Float, val y: Float, val type: String) : Reading
}

/** A sensor emission: either a mapped [Reading] or a generic error message. */
sealed interface SensorUpdate {
    data class Data(val reading: Reading, val platform: String, val timestamp: Long?) : SensorUpdate

    /**
     * KSensor does not document the field shape of its `*.Error` type, so we surface errors as a
     * generic, already-stringified message (see README "KSensor caveats"). Screens may ignore it,
     * but the abstraction does not pretend errors don't happen.
     */
    data class Error(val message: String) : SensorUpdate
}

/**
 * Abstraction over a live source of sensor readings (DIP). The real binding is [KSensorSource];
 * tests substitute `FakeSensorSource`. Deliberately separate from [StateSource] (ISP) so a screen
 * that only needs sensors never transitively depends on state types, and vice versa.
 */
interface SensorSource {
    /**
     * Returns a cold [Flow] that registers the [types] on first collection and is required to
     * unregister them when collection completes or is cancelled.
     *
     * @param intervalMs optional location update cadence in milliseconds; null uses the library default.
     */
    fun sensors(types: List<SensorType>, intervalMs: Long? = null): Flow<SensorUpdate>
}
