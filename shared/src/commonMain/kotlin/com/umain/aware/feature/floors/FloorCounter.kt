package com.umain.aware.feature.floors

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Counts floors climbed from barometric pressure. The first reading becomes the reference; later
 * readings are converted to a relative altitude (international barometric formula) and divided into
 * floors. Climbing up lowers pressure and raises altitude, giving positive floors. Pure (SRP).
 */
class FloorCounter(private val metersPerFloor: Float = 3f) {
    private var referenceHpa: Float? = null

    var floors: Int = 0
        private set

    /** Feed a barometric pressure reading in hPa. */
    fun onPressure(hPa: Float) {
        val ref = referenceHpa ?: hPa.also { referenceHpa = it }
        val deltaMeters = altitudeMeters(hPa) - altitudeMeters(ref)
        floors = (deltaMeters / metersPerFloor).roundToInt()
    }

    companion object {
        private const val SEA_LEVEL_HPA = 1013.25f

        /** Altitude in metres above the 1013.25 hPa reference for a given pressure. */
        fun altitudeMeters(hPa: Float): Float =
            44_330f * (1f - (hPa / SEA_LEVEL_HPA).pow(1f / 5.255f))
    }
}
