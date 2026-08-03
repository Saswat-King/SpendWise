package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.local.dao.IncomeDao
import com.kingstudio.spendwise.data.local.entity.IncomeEntity
import com.kingstudio.spendwise.data.local.entity.IncomeFrequency
import com.kingstudio.spendwise.data.local.entity.IncomeSource
import com.kingstudio.spendwise.data.model.PeriodComparison
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface IncomeRepository {
    fun getCurrentPeriodIncomes(): Flow<List<IncomeEntity>>
    fun getNormalizedIncomeForPeriod(periodKey: String): Flow<Double>
    fun getNormalizedCurrentMonthlyIncome(): Flow<Double>
    fun getIncomeTrend(): Flow<PeriodComparison>
    suspend fun replaceCurrentPeriodIncomes(entries: List<IncomeEntity>)
    suspend fun ensureCurrentPeriodSalaryExists(): Boolean
    suspend fun saveIncome(income: IncomeEntity): Long
    suspend fun deleteIncome(income: IncomeEntity)
}


class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao) : IncomeRepository {

    override fun getCurrentPeriodIncomes(): Flow<List<IncomeEntity>> =
        incomeDao.getIncomesForPeriod(DateRangeCalculator.currentPeriodKey())

    override fun getNormalizedIncomeForPeriod(periodKey: String): Flow<Double> =
        incomeDao.getIncomesForPeriod(periodKey).map { entries ->
            if(entries.isEmpty()) return@map 0.0
            val frequency = entries.first().frequency
            val total = entries.sumOf { it.amount }
            when(frequency) {
                IncomeFrequency.MONTHLY -> total
                IncomeFrequency.YEARLY -> total / 12
            }
        }

    override fun getNormalizedCurrentMonthlyIncome(): Flow<Double> =
        getNormalizedIncomeForPeriod(DateRangeCalculator.currentPeriodKey())

    override fun getIncomeTrend(): Flow<PeriodComparison> {
        val current = DateRangeCalculator.currentPeriodKey()
        val previous = DateRangeCalculator.previousPeriodKey(current)
        return combine(
            getNormalizedIncomeForPeriod(current),
            getNormalizedIncomeForPeriod(previous)
        ) { curr, prev -> PeriodComparison.calculate(curr,prev) }
    }

    override suspend fun replaceCurrentPeriodIncomes(entries: List<IncomeEntity>) {
        val period = DateRangeCalculator.currentPeriodKey()
        incomeDao.getIncomesForPeriodOnce(period).forEach { incomeDao.deleteIncome(it) }
        entries.forEach { incomeDao.upsertIncome(it.copy(periodKey = period)) }
    }

    override suspend fun ensureCurrentPeriodSalaryExists(): Boolean {
        val period = DateRangeCalculator.currentPeriodKey()
        val hasSalary = incomeDao.getIncomesForPeriodOnce(period)
            .any { it.source == IncomeSource.SALARY }
        if(hasSalary) return false
        val previousSalary = incomeDao.getMostRecentSalaryBeforePeriod(period) ?: return false
        incomeDao.upsertIncome(
            previousSalary.copy(id = 0, periodKey = period, createdAt = System.currentTimeMillis())
        )
        return true
    }

    override suspend fun saveIncome(income: IncomeEntity): Long = incomeDao.upsertIncome(income)
    override suspend fun deleteIncome(income: IncomeEntity) = incomeDao.deleteIncome(income)
}