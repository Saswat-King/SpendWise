package com.kingstudio.spendwise.ui.expenses

sealed class ExpenseUiEvent {
    object ExpenseSaved : ExpenseUiEvent()
    data class ShowUndoDelete(val expenseId: Long, val expenseTitle: String): ExpenseUiEvent()
}