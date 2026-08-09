package com.kingstudio.spendwise.data.repository


import com.kingstudio.spendwise.data.local.dao.BudgetDao
import com.kingstudio.spendwise.data.local.entity.BudgetEntity
import com.kingstudio.spendwise.data.local.relation.BudgetWithCategory
import com.kingstudio.spendwise.data.model.BudgetOverview
import com.kingstudio.spendwise.data.model.CategoryBudgetProgress
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.util.BudgetProgressCalculator
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

interface BudgetRepository {
    suspend fun getBudgetByCategory(categoryId: Long, periodKey: String = DateRangeCalculator.currentPeriodKey()): BudgetEntity?
    fun getBudgetOverviewForPeriod(periodKey: String): Flow<BudgetOverview>
    fun getCurrentPeriodBudgetOverview(): Flow<BudgetOverview>
    fun getAvailablePeriods(): Flow<List<String>>
    suspend fun ensureCurrentPeriodBudgetsExist(): Boolean
    suspend fun setBudget(categoryId: Long, amount: Double, periodKey: String = DateRangeCalculator.currentPeriodKey())
    // suspend fun saveBudget(categoryId: Long, amount: Double)
    suspend fun deleteBudget(budget: BudgetEntity)
}

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val expenseRepository: ExpenseRepository) : BudgetRepository {

    override suspend fun getBudgetByCategory(categoryId: Long, periodKey: String): BudgetEntity? =
        budgetDao.getBudgetByCategory(categoryId,periodKey)


    override fun getBudgetOverviewForPeriod(periodKey: String): Flow<BudgetOverview> {
        val (start,end) = DateRangeCalculator.epochRangeForPeriodKey(periodKey)
        return combine(
            budgetDao.getBudgetsWithCategoryForPeriod(periodKey),
            expenseRepository.getSpentPerCategory(DateRange.Custom(start,end))
        ) {budgets, spendingList ->
            val spendingMap = spendingList.associateBy { it.categoryId }
            val categoryProgress = budgets.map { budgetWithCategory ->
                val spent = spendingMap[budgetWithCategory.category.id]?.total ?: 0.0
                val percentUsed = BudgetProgressCalculator.calculatePercentUsed(spent, budgetWithCategory.budget.amount)
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
                utilizationPercent = BudgetProgressCalculator.calculatePercentUsed(totalSpent,totalBudget),
                categoryProgress = categoryProgress
            )
        }
    }

    override fun getCurrentPeriodBudgetOverview(): Flow<BudgetOverview>  =
        getBudgetOverviewForPeriod(DateRangeCalculator.currentPeriodKey())

    override fun getAvailablePeriods(): Flow<List<String>> = budgetDao.getAllPeriodKeysWithBudgets()

    override suspend fun ensureCurrentPeriodBudgetsExist(): Boolean {
        val currentPeriod = DateRangeCalculator.currentPeriodKey()
        if(budgetDao.getBudgetsForPeriodOnce(currentPeriod).isNotEmpty()) return false

        val previousPeriod = budgetDao.getLatestPeriodKeyBefore(currentPeriod) ?: return false
        val previousBudgets = budgetDao.getBudgetsForPeriodOnce(previousPeriod)
        if(previousBudgets.isEmpty()) return false

        previousBudgets.forEach { old ->
            budgetDao.upsertBudget(
                BudgetEntity(categoryId = old.categoryId, periodKey = currentPeriod, amount = old.amount)
            )
        }
        return true
    }


    override suspend fun setBudget(categoryId: Long, amount: Double, periodKey: String) {
        budgetDao.upsertBudget(
            BudgetEntity(categoryId = categoryId, periodKey = periodKey, amount = amount)
        )
    }

//        override suspend fun saveBudget(categoryId: Long, amount: Double) {
//        budgetDao.upsertBudget(BudgetEntity(categoryId = categoryId, amount = amount))
//     }

    override suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)
}