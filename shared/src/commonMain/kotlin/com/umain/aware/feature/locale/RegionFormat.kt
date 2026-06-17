package com.umain.aware.feature.locale

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Maps a country code to a currency symbol and formats amounts with it. Deliberately avoids
 * platform locale/number APIs so it is pure and identical on both targets (SRP).
 */
object RegionFormat {

    fun currencySymbol(countryCode: String): String = when (countryCode.uppercase()) {
        "US" -> "$"
        "GB" -> "£"
        "JP" -> "¥"
        "BR" -> "R$"
        "IN" -> "₹"
        "SE", "NO", "DK" -> "kr"
        "DE", "FR", "ES", "IT", "NL", "IE", "PT", "FI", "AT", "BE", "GR" -> "€"
        else -> "¤"
    }

    /** Format [amount] with the country's currency symbol and exactly two decimal places. */
    fun format(amount: Double, countryCode: String): String {
        val symbol = currencySymbol(countryCode)
        val cents = (amount * 100).roundToLong()
        val sign = if (cents < 0) "-" else ""
        val absCents = abs(cents)
        val whole = absCents / 100
        val frac = (absCents % 100).toString().padStart(2, '0')
        return "$sign$symbol$whole.$frac"
    }
}
