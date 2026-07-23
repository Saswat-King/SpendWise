package com.kingstudio.spendwise.ui.expenses.list

import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.model.PeriodComparison

sealed class ExpenseListItem {
    data class DateHeader(val label: String, val dayTotal: Double) : ExpenseListItem()
    data class Row(val expense: ExpenseWithCategory) : ExpenseListItem()
}


data class ExpensesSummary(
    val totalAmount: Double,
    val trend: PeriodComparison,
    val highestExpense: ExpenseWithCategory?,
    val lowestExpense: ExpenseWithCategory?
)

sealed class ExpenseListUiState {
    object Loading : ExpenseListUiState()
    data class Success(
        val summary: ExpensesSummary, val groupedItems: List<ExpenseListItem>
    ) : ExpenseListUiState()

    object Empty : ExpenseListUiState()
}
