package com.kingstudio.spendwise.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long) : CategoryEntity?

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpenseCountForCategory(categoryId: Long): Int

    @Upsert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Upsert
    suspend fun insertCategories(category: List<CategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}