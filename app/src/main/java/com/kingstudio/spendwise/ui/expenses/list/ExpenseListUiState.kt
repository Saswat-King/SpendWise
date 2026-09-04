package com.kingstudio.spendwise.ui.expenses.list

import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.model.PeriodComparison

data class ExpenseGroup(
    val dateLabel: String,
    val totalAmount: Double,
    val expenses: List<ExpenseWithCategory>
)


data class ExpensesSummary(
    val totalAmount: Double,
    val trend: PeriodComparison,
    val highestExpense: ExpenseWithCategory?,
    val lowestExpense: ExpenseWithCategory?
)

sealed class ExpenseListUiState {
    object Loading : ExpenseListUiState()
    data class Success(
       val groups: List<ExpenseGroup>, val summary: ExpensesSummary
    ) : ExpenseListUiState()

    object Empty : ExpenseListUiState()
}
