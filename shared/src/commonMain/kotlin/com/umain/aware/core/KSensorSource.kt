package com.umain.aware.core

// ─── CENTRALISED KSENSOR IMPORT ──────────────────────────────────────────────────────────────
// KSensor's public type package is NOT documented (see README "KSensor caveats" #1). Every KSensor
// import lives in this one grouped block (and the equivalent in KStateSource.kt) so that, if the
// real package turns out to differ, the fix is confined to these lines — nothing else in Aware
// touches a KSensor type. The two `as K…` aliases resolve the deliberate name overlap between
// KSensor's types and Aware's same-named core types so both can be referenced unambiguously.
import io.github.shadadman.ksensor.KSensor
import io.github.shadadman.ksensor.SensorType as KSensorType
import io.github.shadadman.ksensor.SensorUpdate as KSensorUpdate
import io.github.shadadman.ksensor.Accelerometer
import io.github.shadadman.ksensor.Gyroscope
import io.github.shadadman.ksensor.Magnetometer
import io.github.shadadman.ksensor.Barometer
import io.github.shadadman.ksensor.StepCounter
import io.github.shadadman.ksensor.StepDetector
import io.github.shadadman.ksensor.Location
import io.github.shadadman.ksensor.Orientation
import io.github.shadadman.ksensor.Proximity
import io.github.shadadman.ksensor.LightIlluminance
import io.github.shadadman.ksensor.TouchGestures
// ─────────────────────────────────────────────────────────────────────────────────────────────
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

/**
 * The real [SensorSource]: wraps the global `KSensor` object.
 *
 * Lifecycle contract: the returned flow is cold. `KSensor.registerSensors` registers on first
 * collection; [onCompletion] guarantees `unregisterSensors` runs when the collector completes or is
 * cancelled (e.g. a screen leaving composition), so no listener is ever leaked.
 */
class KSensorSource : SensorSource {

    override fun sensors(types: List<SensorType>, intervalMs: Long?): Flow<SensorUpdate> {
        val ksTypes = types.map { it.toKSensor() }
        val upstream =
            if (intervalMs != null) KSensor.registerSensors(ksTypes, intervalMs)
            else KSensor.registerSensors(ksTypes)
        return upstream
            .map { it.toDomain() }
            .onCompletion { KSensor.unregisterSensors(ksTypes) }
    }
}

private fun SensorType.toKSensor(): KSensorType = when (this) {
    SensorType.ACCELEROMETER -> KSensorType.ACCELEROMETER
    SensorType.GYROSCOPE -> KSensorType.GYROSCOPE
    SensorType.MAGNETOMETER -> KSensorType.MAGNETOMETER
    SensorType.BAROMETER -> KSensorType.BAROMETER
    SensorType.STEP_COUNTER -> KSensorType.STEP_COUNTER
    SensorType.STEP_DETECTOR -> KSensorType.STEP_DETECTOR
    SensorType.LOCATION -> KSensorType.LOCATION
    SensorType.DEVICE_ORIENTATION -> KSensorType.DEVICE_ORIENTATION
    SensorType.PROXIMITY -> KSensorType.PROXIMITY
    SensorType.LIGHT -> KSensorType.LIGHT
    SensorType.TOUCH_GESTURES -> KSensorType.TOUCH_GESTURES
}

private fun KSensorUpdate.toDomain(): SensorUpdate = when (this) {
    is KSensorUpdate.Data -> {
        val reading: Reading? = when (val d = data) {
            is Accelerometer -> Reading.Accelerometer(d.x, d.y, d.z)
            is Gyroscope -> Reading.Gyroscope(d.x, d.y, d.z)
            is Magnetometer -> Reading.Magnetometer(d.x, d.y, d.z)
            is Barometer -> Reading.Barometer(d.pressure)
            is StepCounter -> Reading.StepCount(d.steps)
            is StepDetector -> Reading.StepTick
            is Location -> Reading.Location(d.lat, d.lon, d.alt)
            is Orientation -> Reading.Orientation(d.orientation.toString(), d.orientationInt)
            is Proximity -> Reading.Proximity(d.distanceInCM, d.isNear)
            is LightIlluminance -> Reading.Light(d.illuminance)
            is TouchGestures -> Reading.Touch(d.x, d.y, d.type.toString())
            else -> null
        }
        if (reading != null) {
            SensorUpdate.Data(reading, platformType.toString(), timestamp)
        } else {
            SensorUpdate.Error("Unmapped KSensor reading: $data")
        }
    }
    // Error field shape is undocumented (caveat #2): forward toString() as a generic message.
    is KSensorUpdate.Error -> SensorUpdate.Error(this.toString())
}
