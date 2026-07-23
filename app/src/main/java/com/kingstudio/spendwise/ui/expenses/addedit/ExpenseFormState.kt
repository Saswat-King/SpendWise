package com.kingstudio.spendwise.ui.expenses.addedit

enum class ExpenseFormMode { ADD, EDIT }

data class ExpenseFormState (
    val mode: ExpenseFormMode = ExpenseFormMode.ADD,
    val expenseId: Long? = null,
    val title: String = "",
    val amount: String = "",
    val categoryId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val isSaving: Boolean = false,

    val screenTitle: String = "Add Expense",
    val buttonLabel: String = "Save Expense"
)
