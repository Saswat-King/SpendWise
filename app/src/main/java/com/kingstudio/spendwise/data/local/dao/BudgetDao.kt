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

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity?

    @Query("SELECT COALESCE(SUM(amount),0.0) FROM budgets")
    fun getTotalBudgetAmount(): Flow<Double>

    @Upsert
    suspend fun upsertBudget(budget: BudgetEntity): Long

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}