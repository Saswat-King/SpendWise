package com.kingstudio.spendwise.ui.common

import androidx.core.graphics.toColorInt
import com.kingstudio.spendwise.data.model.BudgetZone

fun BudgetZone.indicatorColor(): Int = when(this) {
    BudgetZone.SAFE -> "#4CAF50".toColorInt()
    BudgetZone.CAUTION -> "#FFA726".toColorInt()
    BudgetZone.DANGER -> "#EF5350".toColorInt()
}