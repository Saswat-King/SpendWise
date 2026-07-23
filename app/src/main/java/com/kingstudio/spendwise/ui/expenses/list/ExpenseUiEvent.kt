package com.kingstudio.spendwise.ui.expenses.list

sealed class ExpenseUiEvent {
    object ExpenseSaved : ExpenseUiEvent()
    data class ShowUndoDelete(val expenseId: Long, val expenseTitle: String): ExpenseUiEvent()
}