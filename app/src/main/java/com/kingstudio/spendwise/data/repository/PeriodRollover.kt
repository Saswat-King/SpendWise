package com.kingstudio.spendwise.data.repository

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeriodRollover @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val budgetRepository: BudgetRepository
) {
    private val mutex = Mutex()

    suspend fun ensureCurrentPeriod() {
        withContext(NonCancellable) {
            mutex.withLock {
                incomeRepository.ensureCurrentPeriodSalaryExists()
                budgetRepository.ensureCurrentPeriodBudgetsExist()
            }
        }
    }

}