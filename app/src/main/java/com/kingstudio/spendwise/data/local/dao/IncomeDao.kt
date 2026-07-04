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

    @Upsert
    suspend fun upsertIncome(income: IncomeEntity): Long

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

}