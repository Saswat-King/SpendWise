package com.kingstudio.spendwise.ui.budget.overview

import com.kingstudio.spendwise.data.model.BudgetOverview
import com.kingstudio.spendwise.data.model.BudgetZone
import com.kingstudio.spendwise.data.util.BudgetPerformanceCalculator

data class CategorySpendShare(
    val categoryName: String,
    val colorHex: String,
    val spentAmount: Double,
    val percentOfTotalSpent: Int
)

data class BudgetCategoryRow(
    val categoryName: String,
    val iconKey: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentUsed: Int,
    val zone: BudgetZone,
    val statusLabel: String
)

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

fun BudgetZone.toStatusLabel(): String = when(this) {
    BudgetZone.SAFE -> "On Track"
    BudgetZone.CAUTION -> "Near Limit"
    BudgetZone.DANGER -> "Over Budget"
}