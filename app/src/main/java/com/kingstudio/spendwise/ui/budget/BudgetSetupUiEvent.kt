package com.kingstudio.spendwise.ui.budget

sealed class BudgetSetupUiEvent {
    object BudgetSaved : BudgetSetupUiEvent()
    object NavigateToAddCategory : BudgetSetupUiEvent()
}