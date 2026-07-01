package com.kingstudio.spendwise.data.model

import com.kingstudio.spendwise.data.local.entity.CategoryEntity

data class BudgetOverview(
    val totalBudget: Double,
    val totalSpent: Double,
    val remaining: Double,
    val utilizationPercent: Int,
    val categoryProgress: List<CategoryBudgetProgress>
)

data class CategoryBudgetProgress(
    val category: CategoryEntity,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentUsed: Int,
    val zone: BudgetZone
)

enum class BudgetZone { SAFE, CAUTION, DANGER }
