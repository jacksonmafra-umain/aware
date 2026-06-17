package com.umain.aware.feature.light

/**
 * Decides light vs dark reading theme from ambient lux, with hysteresis: it only switches to dark
 * once lux drops to [darkBelowLux] or below, and only back to light once lux rises to
 * [lightAboveLux] or above. The gap between the two thresholds prevents flicker when the light is
 * hovering near a single cutoff. Pure (SRP).
 */
class ThemeDecider(
    private val darkBelowLux: Float = 10f,
    private val lightAboveLux: Float = 50f,
    initiallyDark: Boolean = false,
) {
    var isDark: Boolean = initiallyDark
        private set

    /** Feed a lux reading; returns the (possibly updated) dark-mode decision. */
    fun onLux(lux: Float): Boolean {
        if (isDark && lux >= lightAboveLux) {
            isDark = false
        } else if (!isDark && lux <= darkBelowLux) {
            isDark = true
        }
        return isDark
    }
}
