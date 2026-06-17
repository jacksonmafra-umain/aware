package com.umain.aware

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestRuntimePermissions()

        setContent {
            App()
        }
    }

    /**
     * KSensor declares no runtime permissions, so we request the ones its sensors/states need:
     * ACTIVITY_RECOGNITION for the step counter (API 29+), and BLUETOOTH_SCAN / BLUETOOTH_CONNECT for
     * the Nearby devices BLE scan and connection query (API 31+). Location for the Trip tracker is
     * handled separately by KSensor's own AskPermission composable.
     */
    private fun requestRuntimePermissions() {
        val wanted = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        val missing = wanted.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 1)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
