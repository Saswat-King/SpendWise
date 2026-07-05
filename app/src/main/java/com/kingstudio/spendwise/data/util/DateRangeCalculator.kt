package com.kingstudio.spendwise.data.util

import com.kingstudio.spendwise.data.model.DateRange
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.Instant

object DateRangeCalculator {

    private val zone = ZoneId.systemDefault()

    fun toEpochRange(range: DateRange): Pair<Long,Long> = when(range) {
        is DateRange.ThisMonth -> {
            val today = LocalDate.now(zone)
            today.withDayOfMonth(1).toStartOfDayMillis() to
                    today.withDayOfMonth(today.lengthOfMonth()).toEndOfDayMillis()
        }
        is DateRange.ThisYear -> {
            val today = LocalDate.now(zone)
            today.withDayOfYear(1).toStartOfDayMillis() to
                    today.withDayOfYear(today.lengthOfYear()).toEndOfDayMillis()
        }
        is DateRange.Custom -> range.startDate to range.endDate
    }

    fun toPreviousEpochRange(range: DateRange): Pair<Long,Long> = when(range) {

        is DateRange.ThisMonth -> {
            val lastMonth = LocalDate.now(zone).minusMonths(1)
            lastMonth.withDayOfMonth(1).toStartOfDayMillis() to
                    lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).toEndOfDayMillis()
        }
        is DateRange.ThisYear -> {
            val lastYear = LocalDate.now(zone).minusYears(1)
            lastYear.withDayOfYear(1).toStartOfDayMillis() to
                    lastYear.withDayOfYear(lastYear.lengthOfYear()).toEndOfDayMillis()
        }
        is DateRange.Custom -> {
            val durationDays = ChronoUnit.DAYS.between(
                range.startDate.toLocalDate(), range.endDate.toLocalDate()
            ) + 1
            val previousEnd = range.startDate.toLocalDate().minusDays(1)
            val previousStart = previousEnd.minusDays(durationDays - 1)
            previousStart.toStartOfDayMillis() to previousEnd.toEndOfDayMillis()
        }
    }

    private fun LocalDate.toStartOfDayMillis(): Long =
        atStartOfDay(zone).toInstant().toEpochMilli()

    private fun LocalDate.toEndOfDayMillis():  Long =
        plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
}