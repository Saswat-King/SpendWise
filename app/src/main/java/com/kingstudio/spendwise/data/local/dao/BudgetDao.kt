package com.kingstudio.spendwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kingstudio.spendwise.data.local.entity.BudgetEntity
import com.kingstudio.spendwise.data.local.relation.BudgetWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {


    @Transaction
    @Query("SELECT * FROM budgets")
    fun getAllBudgetsWithCategory(): Flow<List<BudgetWithCategory>>

    @Transaction
    @Query("SELECT * FROM budgets WHERE periodKey = :periodKey")
    fun getBudgetsWithCategoryForPeriod(periodKey: String): Flow<List<BudgetWithCategory>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND periodKey = :periodKey")
    suspend fun getBudgetByCategory(categoryId: Long, periodKey: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE periodKey = :periodKey")
    suspend fun getBudgetsForPeriodOnce(periodKey: String): List<BudgetEntity>

    @Query("""SELECT DISTINCT periodKey FROM budgets WHERE periodKey < :beforePeriodKey
        ORDER BY periodKey DESC LIMIT 1""")
    suspend fun getLatestPeriodKeyBefore(beforePeriodKey: String): String?

    @Query("SELECT COALESCE(SUM(amount),0.0) FROM budgets")
    fun getTotalBudgetAmount(): Flow<Double>

    @Upsert
    suspend fun upsertBudget(budget: BudgetEntity): Long

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}