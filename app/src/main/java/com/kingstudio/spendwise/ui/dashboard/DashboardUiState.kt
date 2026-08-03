package com.kingstudio.spendwise.ui.dashboard

import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.model.BudgetOverview
import com.kingstudio.spendwise.data.model.PeriodComparison

data class DashboardUiState(
    val greeting: String,

    val availableBalance: Double,
    val balanceTrend: PeriodComparison,

    val income: Double,
    val incomeTrend: PeriodComparison,

    val expenses: Double,
    val expenseTrend: PeriodComparison,

    val spendingTrendPoints: List<ExpenseDao.DailyTotal>,

    val budgetOverview: BudgetOverview?,

    val recentExpenses: List<ExpenseWithCategory>
)

sealed class DashboardScreenState {
    object Loading : DashboardScreenState()
    data class Success(val data: DashboardUiState) : DashboardScreenState()
}
