package com.kingstudio.spendwise.data.util

import com.kingstudio.spendwise.ui.common.SupportedCurrencies
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {

    fun symbolFor(currencyCode: String): String =
        SupportedCurrencies.list.find { it.code == currencyCode }?.symbol ?: "₹"

    fun formatPlain(amount: Double, currencyCode: String, decimalPlaces: Int = 2): String {
        val symbol = symbolFor(currencyCode)
        val locale = if(currencyCode == "INR") Locale.Builder()
            .setLanguage("en")
            .setRegion("IN")
            .build()
        else {
            Locale.US
        }

        val numberFormat = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = decimalPlaces
            maximumFractionDigits = decimalPlaces
        }

        val sign = if(amount < 0) "-" else ""
        return "$sign$symbol${numberFormat.format(amount)}"
    }


    fun formatCompact(amount: Double, currencyCode: String): String {
        val symbol = symbolFor(currencyCode)
        val absAmount = abs(amount)
        val sign = if (amount < 0) "-" else ""

        if(absAmount < 1_000) return formatPlain(amount, currencyCode, decimalPlaces = 0)


        val compact = if(currencyCode == "INR") {
            when {
                absAmount < 100_000 -> "${trimTrailingZero(absAmount / 1_000)}K"
                absAmount < 10_000_000 -> "${trimTrailingZero(absAmount / 100_000)}L"
                else -> "${trimTrailingZero(absAmount / 10_000_000)}Cr"
            }
        }
            else {
                when {
                    absAmount < 1_000_000 -> "${trimTrailingZero(absAmount / 1_000)}K"
                    absAmount < 1_000_000_000 -> "${trimTrailingZero(absAmount / 1_000_000)}M"
                    else -> "${trimTrailingZero(absAmount / 1_000_000_000)}B"
                }
            }
            return "$sign$symbol$compact"
    }

    private fun trimTrailingZero(value: Double): String {
        val rounded = "%.1f".format(value)
        return if(rounded.endsWith(".0")) rounded.dropLast(2) else rounded
    }
}