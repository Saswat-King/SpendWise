package com.kingstudio.spendwise.ui.budget.overview

import com.kingstudio.spendwise.data.model.BudgetOverview
import com.kingstudio.spendwise.data.model.BudgetZone
import com.kingstudio.spendwise.data.util.BudgetPerformanceCalculator

data class CategorySpendShare(
    val categoryName: String,
    val iconKey: String,
    val colorHex: String,
    val spentAmount: Double,
    val percentOfTotalSpent: Int,
    val categoryCount: Int = 1
)

data class BudgetCategoryRow(
    val categoryName: String,
    val iconKey: String,
    val colorHex: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentUsed: Int,
    val zone: BudgetZone,
//    val statusLabel: String
) {
    val isOver: Boolean get() = remainingAmount < -MONEY_EPSILON
    val isFullyUsed: Boolean get() = !isOver && remainingAmount <= MONEY_EPSILON

    val displayPercent: Int get() = when {
        isOver || isFullyUsed -> 100
        budgetAmount <= 0.0 -> 0
        else -> ((spentAmount / budgetAmount) * 100).toInt()
    }
}

private const val MONEY_EPSILON = 0.005


data class BudgetScreenUiState(
    val selectedPeriodKey: String,
    val availablePeriods: List<String>,
    val overview: BudgetOverview,
    val spendShares: List<CategorySpendShare>,
    val performancePoints: List<BudgetPerformanceCalculator.Point>,
    val categoryRows: List<BudgetCategoryRow>
)

sealed class BudgetScreenState {
    object Loading : BudgetScreenState()
    data class Success(val data: BudgetScreenUiState) : BudgetScreenState()
    object Empty : BudgetScreenState()
}

//fun BudgetZone.toStatusLabel(): String = when(this) {
//    BudgetZone.SAFE -> "On Track"
//    BudgetZone.CAUTION -> "Near Limit"
//    BudgetZone.DANGER -> "Over Budget"
//}