package com.umain.aware.core

import androidx.compose.runtime.Composable
// ─── CENTRALISED KSENSOR IMPORT (the third and last file that touches KSensor) ───────────────
import org.kmp.ksensor.sensor.KSensor
import org.kmp.ksensor.permission.PermissionStatus
import org.kmp.ksensor.permission.PermissionType
// ─────────────────────────────────────────────────────────────────────────────────────────────

/**
 * Aware-owned wrapper around KSensor's location-permission flow, so feature screens request
 * permission through this composable instead of importing KSensor (DIP — keeps the library hidden).
 *
 * @param onResult invoked with `true` when permission is granted, `false` otherwise.
 */
@Composable
fun RequestLocationPermission(onResult: (granted: Boolean) -> Unit) {
    KSensor.AskPermission(PermissionType.LOCATION) { status ->
        onResult(status == PermissionStatus.GRANTED)
    }
}
