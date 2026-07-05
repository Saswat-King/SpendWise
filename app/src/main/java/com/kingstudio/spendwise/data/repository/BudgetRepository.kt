package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.local.dao.BudgetDao
import com.kingstudio.spendwise.data.local.entity.BudgetEntity
import com.kingstudio.spendwise.data.local.relation.BudgetWithCategory
import com.kingstudio.spendwise.data.model.BudgetOverview
import com.kingstudio.spendwise.data.model.CategoryBudgetProgress
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.util.BudgetProgressCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

interface BudgetRepository {
    fun getAllBudgetsWithCategory(): Flow<List<BudgetWithCategory>>
    suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity?
    fun getBudgetOverview(range: DateRange): Flow<BudgetOverview>
    suspend fun saveBudget(categoryId: Long, amount: Double)
    suspend fun deleteBudget(budget: BudgetEntity)
}

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val expenseRepository: ExpenseRepository) : BudgetRepository {

    override fun getAllBudgetsWithCategory(): Flow<List<BudgetWithCategory>> =
        budgetDao.getAllBudgetsWithCategory()

    override suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity? =
        budgetDao.getBudgetByCategory(categoryId)

    override fun getBudgetOverview(range: DateRange): Flow<BudgetOverview> =
        combine(
            budgetDao.getAllBudgetsWithCategory(),
            expenseRepository.getSpentPerCategory(range),
        ) { budgets, spendingList -> val spendingMap = spendingList.associateBy { it.categoryId }

            val categoryProgress = budgets.map { budgetWithCategory ->
                val spent = spendingMap[budgetWithCategory.category.id]?.total ?: 0.0
                val percentUsed = BudgetProgressCalculator.calculatePercentUsed(
                    spent,budgetWithCategory.budget.amount)
                CategoryBudgetProgress(
                    category = budgetWithCategory.category,
                    budgetAmount = budgetWithCategory.budget.amount,
                    spentAmount = spent,
                    remainingAmount = budgetWithCategory.budget.amount - spent,
                    percentUsed = percentUsed,
                    zone = BudgetProgressCalculator.calculateZone(percentUsed)
                )
            }
            val totalBudget = categoryProgress.sumOf { it.budgetAmount }
            val totalSpent = categoryProgress.sumOf { it.spentAmount }

            BudgetOverview(
                totalBudget = totalBudget,
                totalSpent = totalSpent,
                remaining = totalBudget - totalSpent,
                utilizationPercent = BudgetProgressCalculator.calculatePercentUsed(totalSpent, totalBudget),
                categoryProgress = categoryProgress
            )
        }

    override suspend fun saveBudget(categoryId: Long, amount: Double) {
        budgetDao.upsertBudget(BudgetEntity(categoryId = categoryId, amount = amount))
    }

    override suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)
}