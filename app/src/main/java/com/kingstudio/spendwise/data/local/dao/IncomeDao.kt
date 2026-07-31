package com.kingstudio.spendwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.kingstudio.spendwise.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Query("SELECT * FROM incomes ORDER BY createdAt DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM incomes WHERE frequency = 'MONTHLY' ")
    fun getTotalMonthlyIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount),0.0) FROM incomes WHERE frequency = 'YEARLY' ")
    fun getTotalYearlyIncome(): Flow<Double>

    @Query("SELECT * FROM incomes WHERE periodKey = :periodKey ORDER BY createdAt DESC")
    fun getIncomesForPeriod(periodKey: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE periodKey = :periodKey")
    suspend fun getIncomesForPeriodOnce(periodKey: String): List<IncomeEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0 ) FROM incomes WHERE periodKey = :periodKey AND frequency = 'MONTHLY'")
    fun getTotalMonthlyIncomeForPeriod(periodKey: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0 ) FROM incomes WHERE periodKey = :periodKey AND frequency = 'YEARLY'")
    fun getTotalYearlyIncomeForPeriod(periodKey: String): Flow<Double>

    @Query("""SELECT * FROM incomes WHERE source = 'SALARY' AND periodKey < :beforePeriodKey ORDER BY periodKey DESC LIMIT 1""")
    suspend fun getMostRecentSalaryBeforePeriod(beforePeriodKey: String): IncomeEntity?

    @Upsert
    suspend fun upsertIncome(income: IncomeEntity): Long

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

}