package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.local.dao.IncomeDao
import com.kingstudio.spendwise.data.local.entity.IncomeEntity
import com.kingstudio.spendwise.data.local.entity.IncomeSource
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

interface IncomeRepository {
    fun getAllIncome(): Flow<List<IncomeEntity>>
    fun getCurrentPeriodIncomes(): Flow<List<IncomeEntity>>
    fun getNormalizedMonthlyIncome(): Flow<Double>
    fun getNormalizedCurrentMonthlyIncome(): Flow<Double>
    suspend fun replaceCurrentPeriodIncomes(entries: List<IncomeEntity>)
    suspend fun ensureCurrentPeriodSalaryExists(): Boolean
    suspend fun saveIncome(income: IncomeEntity): Long
    suspend fun deleteIncome(income: IncomeEntity)
}


class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao) : IncomeRepository {

    override fun getAllIncome(): Flow<List<IncomeEntity>> = incomeDao.getAllIncomes()

    override fun getCurrentPeriodIncomes(): Flow<List<IncomeEntity>> =
        incomeDao.getIncomesForPeriod(DateRangeCalculator.currentPeriodKey())

    override fun getNormalizedMonthlyIncome(): Flow<Double> = combine(
        incomeDao.getTotalMonthlyIncome(), incomeDao.getTotalYearlyIncome()) {
            monthly, yearly -> monthly + (yearly / 12)
    }

    override fun getNormalizedCurrentMonthlyIncome(): Flow<Double> {
        val period = DateRangeCalculator.currentPeriodKey()
        return combine(
            incomeDao.getTotalMonthlyIncomeForPeriod(period),
            incomeDao.getTotalYearlyIncomeForPeriod(period)
        ) { monthly, yearly -> monthly + (yearly / 12) }
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