package com.kingstudio.spendwise.data.util

import com.kingstudio.spendwise.data.model.BudgetZone
import kotlin.math.roundToInt

object BudgetProgressCalculator {
    fun calculatePercentUsed(spent: Double, budget: Double): Int {
        if(budget <= 0.0) return 0
        return ((spent / budget) * 100).roundToInt()
    }
    fun calculateZone(percentUsed: Int): BudgetZone = when{
        percentUsed < 60 -> BudgetZone.SAFE
        percentUsed in 60..80 -> BudgetZone.CAUTION
        else -> BudgetZone.DANGER
    }
}