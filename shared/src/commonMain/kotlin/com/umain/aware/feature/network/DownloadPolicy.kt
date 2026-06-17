package com.umain.aware.feature.network

/** The download behaviour a media app would pick for the current network. */
enum class DownloadQuality { PAUSED, SD, HD }

/**
 * Decides download quality from connectivity + the active network: offline pauses downloads, Wi-Fi
 * allows HD, anything else (cellular) drops to SD to save data. The network is matched by string so
 * it is robust to however the platform labels the connection. Pure (SRP).
 */
object DownloadPolicy {
    fun decide(isConnected: Boolean, network: String): DownloadQuality = when {
        !isConnected -> DownloadQuality.PAUSED
        network.contains("wifi", ignoreCase = true) -> DownloadQuality.HD
        else -> DownloadQuality.SD
    }
}
