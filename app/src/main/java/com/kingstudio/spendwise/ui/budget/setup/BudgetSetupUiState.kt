package com.kingstudio.spendwise.ui.budget.setup

import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.ui.common.FormMode

data class BudgetCategoryInput(
    val category: CategoryEntity,
    val amount: String = ""
)

data class BudgetSetupFormState(
    val mode: FormMode = FormMode.SETUP,
    val categoryInputs: List<BudgetCategoryInput> = emptyList(),
    val isSaving: Boolean = false,
    val screenTitle: String = "Create Your Budget",
    val buttonLabel: String = "Continue"
)

data class BudgetSetupSummary(val totalBudget: Double)