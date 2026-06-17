package com.umain.aware.feature.locale

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionFormatTest {

    @Test
    fun maps_known_countries_to_symbols() {
        assertEquals("$", RegionFormat.currencySymbol("US"))
        assertEquals("£", RegionFormat.currencySymbol("GB"))
        assertEquals("¥", RegionFormat.currencySymbol("JP"))
        assertEquals("€", RegionFormat.currencySymbol("DE"))
        assertEquals("R$", RegionFormat.currencySymbol("br")) // case-insensitive
    }

    @Test
    fun unknown_country_falls_back_to_generic_currency_sign() {
        assertEquals("¤", RegionFormat.currencySymbol("ZZ"))
    }

    @Test
    fun formats_amount_with_two_decimals() {
        assertEquals("$1234.50", RegionFormat.format(1234.5, "US"))
        assertEquals("€0.09", RegionFormat.format(0.09, "DE"))
    }
}
