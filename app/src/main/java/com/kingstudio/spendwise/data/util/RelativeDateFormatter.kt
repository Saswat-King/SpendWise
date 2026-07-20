package com.kingstudio.spendwise.data.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object RelativeDateFormatter {

    private val zone = ZoneId.systemDefault()
    private val sameYearFormat = DateTimeFormatter.ofPattern("MMMM d")
    private val otherYearFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    fun format(epochMillis: Long): String {
        val date = java.time.Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)

        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            date.year == today.year -> date.format(sameYearFormat)
            else -> date.format(otherYearFormat)
        }
    }

    fun toLocalDate(epochMillis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
}