package com.kingstudio.spendwise.ui.budget.setup

sealed class BudgetSetupUiEvent {
    object BudgetSaved : BudgetSetupUiEvent()
    object NavigateToAddCategory : BudgetSetupUiEvent()
}