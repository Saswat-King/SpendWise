package com.kingstudio.spendwise.data.util

import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object BudgetPerformanceCalculator {

    data class Point(
        val dayOfMonth: Int,
        val cumulativeBudget: Double,
        val cumulativeActual: Double?
    )

    private val zone = ZoneId.systemDefault()

    fun calculate(
        periodKey: String,
        totalBudget: Double,
        dailyTotals: List<ExpenseDao.DailyTotal>,
        isCurrentPeriod: Boolean
    ): List<Point> {
        val (year,month) = periodKey.split("-").map { it.toInt() }
        val daysInMonth = LocalDate.of(year,month,1).lengthOfMonth()
        val lastActualDay = if(isCurrentPeriod) {
            LocalDate.now(zone).dayOfMonth.coerceAtMost(daysInMonth)
        }
        else {
            daysInMonth
        }
        val spendByDay: Map<Int, Double> = dailyTotals.associate { daily ->
            val date = Instant.ofEpochMilli(daily.dayTimestamp).atZone(zone).toLocalDate()
            date.dayOfMonth to daily.total
        }

        var runningActual = 0.0
        return (1..daysInMonth).map { day ->
            val budgetSoFar = totalBudget * day / daysInMonth
            val actual = if(day <= lastActualDay) {
                runningActual += spendByDay[day] ?: 0.0
                runningActual
            } else null
            Point(dayOfMonth = day, cumulativeBudget = budgetSoFar, cumulativeActual = actual)
        }
    }
}