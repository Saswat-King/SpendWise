package com.kingstudio.spendwise.ui.common


data class SupportedCurrency(val symbol: String, val code: String, val displayName: String)

object SupportedCurrencies {
    val list = listOf(
        SupportedCurrency("₹", "INR", "Indian Rupee"),
        SupportedCurrency("$", "USD", "US Dollar"),
        SupportedCurrency("€", "EUR", "Euro"),
        SupportedCurrency("£", "GBP", "British Pound"),
        SupportedCurrency("₽", "RUB", "Russian Ruble"),
        SupportedCurrency("¥", "JPY", "Japanese Yen"),
        SupportedCurrency("¥", "CNY", "Chinese Yuan"),
        SupportedCurrency("₩", "KRW", "South Korean Won"),
        SupportedCurrency("CHF", "CHF", "Swiss Franc"),
        SupportedCurrency("A$", "AUD", "Australian Dollar"),
        SupportedCurrency("C$", "CAD", "Canadian Dollar"),
        SupportedCurrency("NZ$", "NZD", "New Zealand Dollar"),
        SupportedCurrency("S$", "SGD", "Singapore Dollar"),
        SupportedCurrency("HK$", "HKD", "Hong Kong Dollar"),
        SupportedCurrency("د.إ", "AED", "UAE Dirham"),
        SupportedCurrency("﷼", "SAR", "Saudi Riyal"),
        SupportedCurrency("฿", "THB", "Thai Baht"),
        SupportedCurrency("Rp", "IDR", "Indonesian Rupiah"),
        SupportedCurrency("RM", "MYR", "Malaysian Ringgit"),
        SupportedCurrency("R", "ZAR", "South African Rand"),
        SupportedCurrency("R$", "BRL", "Brazilian Real"),
        SupportedCurrency("MX$", "MXN", "Mexican Peso"),
        SupportedCurrency("kr", "SEK", "Swedish Krona"),
        SupportedCurrency("kr", "NOK", "Norwegian Krone"),
        SupportedCurrency("kr", "DKK", "Danish Krone"),
        SupportedCurrency("zł", "PLN", "Polish Zloty"),
        SupportedCurrency("₺", "TRY", "Turkish Lira"),
        SupportedCurrency("₫", "VND", "Vietnamese Dong"),
        SupportedCurrency("₱", "PHP", "Philippine Peso")
    )
}