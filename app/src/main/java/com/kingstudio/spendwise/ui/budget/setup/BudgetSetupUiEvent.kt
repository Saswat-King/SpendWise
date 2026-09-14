package com.kingstudio.spendwise.ui.budget.setup

sealed class BudgetSetupUiEvent {
    object BudgetSaved : BudgetSetupUiEvent()
    object ShowEmptyBudgetMessage : BudgetSetupUiEvent()
    object NavigateToAddCategory : BudgetSetupUiEvent()
}