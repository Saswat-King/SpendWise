package com.kingstudio.spendwise.ui.income

import com.kingstudio.spendwise.data.local.entity.IncomeFrequency
import com.kingstudio.spendwise.ui.common.FormMode

data class IncomeFormState(
    val mode: FormMode = FormMode.SETUP,
    val frequency: IncomeFrequency = IncomeFrequency.MONTHLY,

    val primaryIncomeAmount: String = "",
    val bonusAmount: String = "",
    val freelanceAmount: String = "",
    val otherAmount: String = "",

    val primaryIncomeError: String? = null,
    val isSaving: Boolean = false,

    val screenTitle: String = "Income Setup",
    val subTitle: String = "Let's understand your income sources",
    val buttonLabel: String = "Continue"
)

data class IncomeSummary(
    val baseIncome: Double,
    val additionalIncome: Double,
    val totalIncome: Double,
    val periodLabel: String
)