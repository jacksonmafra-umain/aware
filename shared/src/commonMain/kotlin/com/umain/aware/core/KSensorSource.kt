package com.umain.aware.core

// ─── CENTRALISED KSENSOR IMPORT ──────────────────────────────────────────────────────────────
// Every KSensor import lives in this one grouped block (and the equivalents in KStateSource.kt and
// LocationPermission.kt) so the library stays an implementation detail (DIP). The public types live
// under org.kmp.ksensor.* (the Maven coordinate io.github.shadadman:KSensor is unrelated to the
// package name). The two `as K…` aliases resolve the deliberate name overlap with Aware's own
// same-named core types.
import org.kmp.ksensor.sensor.KSensor
import org.kmp.ksensor.sensor.SensorData
import org.kmp.ksensor.sensor.SensorType as KSensorType
import org.kmp.ksensor.sensor.SensorUpdate as KSensorUpdate
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
        // SensorData is sealed, so this maps exhaustively onto Aware's own Reading type.
        val reading: Reading = when (val d = data) {
            is SensorData.Accelerometer -> Reading.Accelerometer(d.x, d.y, d.z)
            is SensorData.Gyroscope -> Reading.Gyroscope(d.x, d.y, d.z)
            is SensorData.Magnetometer -> Reading.Magnetometer(d.x, d.y, d.z)
            is SensorData.Barometer -> Reading.Barometer(d.pressure)
            is SensorData.StepCounter -> Reading.StepCount(d.steps)
            SensorData.StepDetector -> Reading.StepTick
            is SensorData.Location -> Reading.Location(d.latitude, d.longitude, d.altitude)
            is SensorData.Orientation -> Reading.Orientation(d.orientation.name, d.orientationInt)
            is SensorData.Proximity -> Reading.Proximity(d.distanceInCM, d.isNear)
            is SensorData.LightIlluminance -> Reading.Light(d.illuminance)
            is SensorData.TouchGestures -> Reading.Touch(d.x, d.y, d.type.name)
        }
        SensorUpdate.Data(reading, platformType.name, timestamp)
    }
    is KSensorUpdate.Error -> SensorUpdate.Error(exception.message ?: exception.toString())
}
