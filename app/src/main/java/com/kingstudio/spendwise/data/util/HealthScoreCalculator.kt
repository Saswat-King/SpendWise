package com.kingstudio.spendwise.data.util

import com.kingstudio.spendwise.data.model.CategoryBudgetProgress
import com.kingstudio.spendwise.data.model.HealthFactor
import com.kingstudio.spendwise.data.model.HealthScore
import com.kingstudio.spendwise.data.model.HealthStatus
import java.time.LocalDate
import kotlin.math.roundToInt

object HealthScoreCalculator {

    data class Input(
        val monthlyIncome: Double,
        val spentSoFar: Double,
        val spentSamePointLastMonth: Double?,
        val categoryProgress: List<CategoryBudgetProgress>,
        val expenseCount: Int,
        val today: LocalDate
    )

    const val MIN_DAYS = 7
    const val MIN_EXPENSES = 5
    private const val GOOD_FROM = 70
    private const val AVERAGE_FROM = 40

    private const val W_SAVINGS = 40.0
    private const val W_BUDGET = 40.0
    private const val W_TREND = 20.0
    private const val SAVINGS_TARGET = 0.20
    private const val PACE_LIMIT = 1.5
    private const val TREND_LIMIT = 0.30

    private class Part(val factor: HealthFactor, val weight: Double, val quality: Double)


    fun calculate(input: Input): HealthScore? {
        val day = input.today.dayOfMonth
        if(day < MIN_DAYS || input.expenseCount < MIN_EXPENSES) return null

        val elapsed = day.toDouble() / input.today.lengthOfMonth()

        val parts = listOfNotNull(
            savingQuality(input, elapsed)?.let { Part(HealthFactor.SAVINGS, W_SAVINGS, it) },
            budgetQuality(input, elapsed)?.let { Part(HealthFactor.BUDGET, W_BUDGET, it) },
            trendQuality(input)?.let { Part(HealthFactor.TREND, W_TREND, it) }
        )

        if(parts.isEmpty()) return null

        val score = (parts.sumOf { it.weight * it.quality } / parts.sumOf { it.weight } * 100 ).roundToInt()
        val status = when {
            score >= GOOD_FROM -> HealthStatus.GOOD
            score >= AVERAGE_FROM -> HealthStatus.AVERAGE
            else -> HealthStatus.NEEDS_IMPROVEMENT
        }
        return HealthScore(score, status, parts.minBy { it.quality }.factor)
    }

    private fun savingQuality(input: Input, elapsed: Double): Double? {
        if(input.monthlyIncome <= 0.0) return null
        val projectedSpend = input.spentSoFar / elapsed
        val rate = (input.monthlyIncome - projectedSpend) /input.monthlyIncome
        return (rate / SAVINGS_TARGET).coerceIn(0.0, 1.0)
    }

    private fun budgetQuality(input: Input, elapsed: Double): Double? {
        val categories = input.categoryProgress
        val totalBudget = categories.sumOf { it.budgetAmount }
        if(totalBudget <= 0.0) return null

        val paceRatio = categories.sumOf { it.spentAmount } / (totalBudget * elapsed)
        val pace = ((PACE_LIMIT - paceRatio) / (PACE_LIMIT - 1.0)).coerceIn(0.0, 1.0)
        val within  = categories.count { it.spentAmount <= it.budgetAmount }.toDouble() / categories.size
        return 0.6 * pace + 0.4 * within
    }

    private fun trendQuality(input: Input): Double? {
        val previous = input.spentSamePointLastMonth?.takeIf { it > 0.0 } ?: return null
        val change = (input.spentSoFar - previous) / previous
        return if (change <= 0.0) 1.0 else (1.0 - change / TREND_LIMIT).coerceIn(0.0, 1.0)
    }
}