package com.kingstudio.spendwise.data.util

import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.model.InsightFact
import com.kingstudio.spendwise.data.model.InsightType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

object InsightFactsGenerator {

    data class Input(
        val monthlyIncome: Double,
        val thisMonth: List<ExpenseWithCategory>,
        val lastMonth: List<ExpenseWithCategory>,
        val today: LocalDate
    )

    fun generate(input: Input): List<InsightFact> {
        if(input.today.dayOfMonth < HealthScoreCalculator.MIN_DAYS ||
            input.thisMonth.size < HealthScoreCalculator.MIN_EXPENSES
        ) return emptyList()

        return listOfNotNull(
            projection(input)
        )
    }

    fun spentUpToDay(items: List<ExpenseWithCategory>, day: Int): Double =
        items.filter { dateOf(it).dayOfMonth <= day}.sumOf { it.expense.amount }

    private fun dateOf(item: ExpenseWithCategory): LocalDate =
        Instant.ofEpochMilli(item.expense.date).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun isWeekend(d: LocalDate) =
        d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY

    private fun total(items: List<ExpenseWithCategory>) = items.sumOf { it.expense.amount }

    private fun projection(i: Input): InsightFact? {
        if(i.monthlyIncome <= 0.0) return null
        val elapsed = i.today.dayOfMonth.toDouble() / i.today.lengthOfMonth()
        val projected = total(i.thisMonth) / elapsed
        val percent = (projected / i.monthlyIncome * 100).roundToInt()
        return InsightFact(InsightType.PROJECTION, if(percent > 100) 90 else 50, "", projected, percent)
    }

    private fun categoryShift(i: Input): InsightFact? {
        val day = i.today.dayOfMonth
        val now = i.thisMonth.groupBy { it.category.name }
            .mapValues { (_, expenses) -> total(expenses) }

        val before = i.lastMonth.filter { dateOf(it).dayOfMonth <= day }
            .groupBy { it.category.name }
            .mapValues { (_, expenses) -> total(expenses) }
        val monthTotal = now.values.sum()

        return now.mapNotNull { (name, amount) ->
            val prev = before[name] ?: return@mapNotNull null
            val diff = amount - prev
            if(prev <= 0.0 || diff < monthTotal * 0.05 || diff / prev < 0.25) null
            else Triple(name, diff, (diff / prev * 100).roundToInt())
        }.maxByOrNull { it.second }
            ?.let { InsightFact(InsightType.CATEGORY_SHIFT, 70, it.first, it.second, it.third) }
    }


    private fun weekendPattern(i: Input): InsightFact? {
        val days = generateSequence(i.today.withDayOfMonth(1)) { it.plusDays(1) }
            .takeWhile { !it.isAfter(i.today) }.toList()

        val weekendDays = days.count { isWeekend(it) }
        val weekdayDays = days.size - weekendDays
        if(weekendDays < 2 || weekdayDays < 5) return null

        val weekendTotal = total(i.thisMonth.filter { isWeekend(dateOf(it)) })
        val weekdayTotal = total(i.thisMonth) - weekendTotal
        val weekendAvg = weekendTotal / weekendDays
        val weekdayAvg = weekdayTotal / weekdayDays
        if(weekendAvg <= 0.0 || weekdayAvg <= 0.0) return null

        return when {
            weekendAvg >= weekdayAvg * 1.5 -> InsightFact(
                InsightType.WEEKEND_PATTERN, 55, "weekend", weekendAvg,
                ((weekendAvg / weekdayAvg - 1) * 100).roundToInt()
            )

            weekdayAvg >= weekendAvg * 1.5 -> InsightFact(
                InsightType.WEEKEND_PATTERN, 55, "weekday", weekdayAvg,
                ((weekdayAvg / weekendAvg - 1) * 100).roundToInt()
            )
            else -> null
        }
    }


    private fun unusualExpense(i: Input): InsightFact? {
        val amounts = i.thisMonth.map { it.expense.amount }.sorted()
        val n = amounts.size
        val median = if(n % 2 == 1) amounts[n/2] else (amounts[n/2-1] + amounts[n/2]) / 2
        val top = i.thisMonth.maxBy { it.expense.amount }
        val monthTotal = amounts.sum()
        if(median <= 0.0 || top.expense.amount < median * 3 || top.expense.amount < monthTotal * 0.10) return null
        return InsightFact(
            InsightType.UNUSUAL_EXPENSE, 80, top.category.name,
            top.expense.amount, (top.expense.amount / monthTotal * 100).roundToInt()
        )
    }


    private fun noSpendDays(i: Input): InsightFact? {
        val spendDays = i.thisMonth.map { dateOf(it) }.toSet().size
        val free = i.today.dayOfMonth - spendDays
        return if(free >= 3) InsightFact(InsightType.NO_SPEND_DAYS, 40, "",free.toDouble(), 0)
        else null
    }


    private fun smallSpends(i: Input): InsightFact? {
        if(i.monthlyIncome <= 0.0) return null
        val small = i.thisMonth.filter { it.expense.amount < i.monthlyIncome * 0.005 }
        val share = total(small) / total(i.thisMonth)
        return if(small.size >= 8 && share >= 0.10)
            InsightFact(InsightType.SMALL_SPENDS, 45, "", total(small), (share * 100).roundToInt())
        else null
    }
}