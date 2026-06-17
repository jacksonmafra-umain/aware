package com.umain.aware.feature.steps

/**
 * Tracks progress toward a daily step goal. The hardware step counter is cumulative since boot, so
 * the first reading is captured as a [baseline] and everything afterwards is reported as
 * steps-*since-start*. Pure and platform-free (SRP).
 */
class StepSession(val goal: Int = 8_000) {
    private var baseline: Int? = null

    var stepsSinceStart: Int = 0
        private set

    /** Feed the latest cumulative counter value. The first value seen sets the baseline. */
    fun onCounter(cumulativeSteps: Int) {
        val b = baseline
        if (b == null) {
            baseline = cumulativeSteps
            stepsSinceStart = 0
        } else {
            stepsSinceStart = (cumulativeSteps - b).coerceAtLeast(0)
        }
    }

    /** Progress in 0f..1f. */
    val progress: Float
        get() = if (goal <= 0) 0f else (stepsSinceStart.toFloat() / goal).coerceIn(0f, 1f)

    val goalReached: Boolean
        get() = stepsSinceStart >= goal
}
