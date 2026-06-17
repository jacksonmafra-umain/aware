package com.umain.aware.feature.privacy

/**
 * Decides whether sensitive content (e.g. a bank balance) should be hidden. It is hidden whenever
 * the app is not in the foreground, the device is locked, or the screen is off — any one is enough.
 * Pure (SRP).
 */
object ShieldPolicy {
    fun hidden(appVisible: Boolean, locked: Boolean, screenOn: Boolean): Boolean =
        !appVisible || locked || !screenOn
}
