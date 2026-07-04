package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.local.dao.IncomeDao
import com.kingstudio.spendwise.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

interface IncomeRepository {
    fun getAllIncome(): Flow<List<IncomeEntity>>
    fun getNormalizedMonthlyIncome(): Flow<Double>
    suspend fun saveIncome(income: IncomeEntity): Long
    suspend fun deleteIncome(income: IncomeEntity)
}


class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao) : IncomeRepository {

    override fun getAllIncome(): Flow<List<IncomeEntity>> = incomeDao.getAllIncomes()

    override fun getNormalizedMonthlyIncome(): Flow<Double> = combine(
        incomeDao.getTotalMonthlyIncome(), incomeDao.getTotalYearlyIncome()) {
            monthly, yearly -> monthly + (yearly / 12)
    }

    override suspend fun saveIncome(income: IncomeEntity): Long = incomeDao.upsertIncome(income)
    override suspend fun deleteIncome(income: IncomeEntity) = incomeDao.deleteIncome(income)
}