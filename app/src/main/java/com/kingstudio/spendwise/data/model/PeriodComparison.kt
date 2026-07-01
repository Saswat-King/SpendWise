package com.kingstudio.spendwise.data.model

data class PeriodComparison(
    val currentValues: Double,
    val previousValue: Double,
    val percentChange: Double,
    val direction: TrendDirection
) {

    companion object {
        fun calculate(current: Double, previous: Double) : PeriodComparison {
            val percentChange = when {
                previous == 0.0 && current == 0.0 -> 0.0
                previous == 0.0 -> 100.0
                else -> ((current-previous) / previous) * 100
            }
            val direction = when {
                percentChange > 0.0 -> TrendDirection.INCREASE
                percentChange < 0.0 -> TrendDirection.DECREASE
                else -> TrendDirection.NO_CHANGE
            }
            return PeriodComparison(current,previous, percentChange, direction)
        }
    }
}
enum class TrendDirection { INCREASE, DECREASE, NO_CHANGE }
