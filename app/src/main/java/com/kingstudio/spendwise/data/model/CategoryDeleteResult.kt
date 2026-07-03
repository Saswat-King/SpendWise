package com.kingstudio.spendwise.data.model

sealed class CategoryDeleteResult {
    object Success : CategoryDeleteResult()
    data class BlockedByExpenses(val expenseCount: Int) : CategoryDeleteResult()
}