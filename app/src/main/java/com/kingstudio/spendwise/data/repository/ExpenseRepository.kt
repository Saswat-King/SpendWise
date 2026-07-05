package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import com.kingstudio.spendwise.data.local.entity.ExpenseEntity
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.model.PeriodComparison
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject


interface ExpenseRepository {
    fun getRecentExpenses(limit: Int = 5): Flow<List<ExpenseWithCategory>>
    fun getAllExpenses(): Flow<List<ExpenseWithCategory>>
    fun getExpensesForRange(range: DateRange): Flow<List<ExpenseWithCategory>>
    fun getTotalExpensesForRange(range: DateRange): Flow<Double>
    fun getExpenseTrend(range: DateRange): Flow<PeriodComparison>
    fun getHighestExpense(range: DateRange): Flow<ExpenseWithCategory?>
    fun getLowestExpense(range: DateRange): Flow<ExpenseWithCategory?>
    fun getDailySpendingTrend(range: DateRange): Flow<List<ExpenseDao.DailyTotal>>
    fun getSpentPerCategory(range: DateRange): Flow<List<ExpenseDao.CategorySpending>>
    suspend fun saveExpense(expense: ExpenseEntity): Long
    suspend fun deleteExpense(expense: ExpenseEntity)
    suspend fun getExpenseById(id: Long): ExpenseEntity?
}


class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao) : ExpenseRepository {

    override fun getRecentExpenses(limit: Int): Flow<List<ExpenseWithCategory>> =
        expenseDao.getRecentExpensesWithCategory(limit)

    override fun getAllExpenses(): Flow<List<ExpenseWithCategory>> =
        expenseDao.getAllExpensesWithCategory()

    override fun getExpensesForRange(range: DateRange): Flow<List<ExpenseWithCategory>> {
        val (start, end) = DateRangeCalculator.toEpochRange(range)
        return expenseDao.getExpensesWithCategoryByDateRange(start,end)
    }

    override fun getTotalExpensesForRange(range: DateRange): Flow<Double> {
        val (start, end) = DateRangeCalculator.toEpochRange(range)
        return expenseDao.getTotalExpensesByDateRange(start,end)
    }

    override fun getExpenseTrend(range: DateRange): Flow<PeriodComparison> {
        val (currentStart, currentEnd) = DateRangeCalculator.toEpochRange(range)
        val (prevStart, prevEnd ) = DateRangeCalculator.toPreviousEpochRange(range)

        return combine(
            expenseDao.getTotalExpensesByDateRange(currentStart,currentEnd),
            expenseDao.getTotalExpensesByDateRange(prevStart,prevEnd)
        ) { current, previous -> PeriodComparison.calculate(current, previous) }
    }

    override fun getHighestExpense(range: DateRange): Flow<ExpenseWithCategory?> {
        val (start, end) = DateRangeCalculator.toEpochRange(range)
        return expenseDao.getHighestExpenseWithCategoryInPeriod(start, end)
    }

    override fun getLowestExpense(range: DateRange): Flow<ExpenseWithCategory?> {
        val (start, end) = DateRangeCalculator.toEpochRange(range)
        return expenseDao.getLowestExpenseWithCategoryInPeriod(start,end)
    }

    override fun getDailySpendingTrend(range: DateRange): Flow<List<ExpenseDao.DailyTotal>> {
        val (start, end) = DateRangeCalculator.toEpochRange(range)
        return expenseDao.getDailyExpenseTotals(start,end)
    }

    override fun getSpentPerCategory(range: DateRange): Flow<List<ExpenseDao.CategorySpending>> {
        val (start, end) = DateRangeCalculator.toEpochRange(range)
        return expenseDao.getSpentPerCategoryInPeriod(start,end)
    }

    override suspend fun saveExpense(expense: ExpenseEntity): Long = expenseDao.upsertExpense(expense)
    override suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)
    override suspend fun getExpenseById(id: Long): ExpenseEntity? = expenseDao.getExpenseById(id)
}