package com.kingstudio.spendwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kingstudio.spendwise.data.local.entity.ExpenseEntity
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun getRecentExpensesWithCategory(limit: Int = 5): Flow<List<ExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesWithCategory(): Flow<List<ExpenseWithCategory>>

    @Transaction
    @Query("""SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER
        BY date DESC""")
    fun getExpensesWithCategoryByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseWithCategory>>


    @Query("""SELECT COALESCE(SUM(amount),0.0) FROM expenses
        WHERE date >= :startDate AND date <= :endDate""")
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long) : Flow<Double>

    @Query("""SELECT COALESCE(SUM(amount),0.0) FROM expenses WHERE categoryId = :categoryId
        AND date >= :startDate AND date <= :endDate""")
    fun getSpentAmountByCategoryAndDateRange(categoryId: Long, startDate: Long, endDate: Long) : Flow<Double>


    @Query("""SELECT (date/86400000) * 86400000 AS dayTimestamp, 
        COALESCE(SUM(amount),0.0) AS total FROM expenses WHERE date >= :startDate
        AND date <= :endDate GROUP BY dayTimestamp ORDER BY dayTimestamp ASC""")
    fun getDailyExpenseTotals(startDate: Long, endDate: Long) : Flow<List<DailyTotal>>


    @Upsert
    suspend fun saveExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id= :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Transaction
    @Query("""SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate
        ORDER BY amount DESC LIMIT 1""")
    fun getHighestExpenseWithCategoryInPeriod(startDate: Long, endDate: Long): Flow<ExpenseWithCategory?>

    @Transaction
    @Query("""SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate
        ORDER BY amount ASC LIMIT 1""")
    fun getLowestExpenseWithCategoryInPeriod(startDate: Long, endDate: Long): Flow<ExpenseWithCategory?>


    @Query("""SELECT categoryId, COALESCE(SUM(amount),0.0) AS total
          FROM expenses WHERE date >= :startDate AND date <= :endDate
          GROUP BY categoryId""")
    fun getSpendPerCategoryInPeriod(startDate: Long, endDate: Long): Flow<List<CategorySpending>>


    data class DailyTotal(
        val dayTimestamp: Long,
        val total: Double
    )

    data class CategorySpending(
        val categoryId: Long,
        val total: Double
    )
}