package com.umain.aware.feature.network

import kotlin.test.Test
import kotlin.test.assertEquals

class DownloadPolicyTest {

    @Test
    fun offline_pauses_regardless_of_network() {
        assertEquals(DownloadQuality.PAUSED, DownloadPolicy.decide(isConnected = false, network = "WIFI"))
        assertEquals(DownloadQuality.PAUSED, DownloadPolicy.decide(isConnected = false, network = "CELLULAR"))
    }

    @Test
    fun wifi_allows_hd() {
        assertEquals(DownloadQuality.HD, DownloadPolicy.decide(isConnected = true, network = "WiFi"))
        assertEquals(DownloadQuality.HD, DownloadPolicy.decide(isConnected = true, network = "wifi_5ghz"))
    }

    @Test
    fun cellular_or_unknown_drops_to_sd() {
        assertEquals(DownloadQuality.SD, DownloadPolicy.decide(isConnected = true, network = "CELLULAR"))
        assertEquals(DownloadQuality.SD, DownloadPolicy.decide(isConnected = true, network = ""))
    }
}
