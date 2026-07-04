package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.local.dao.CategoryDao
import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.data.model.CategoryDeleteResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(id: Long): CategoryEntity?
    suspend fun saveCategory(category: CategoryEntity): Long
    suspend fun deleteCategory(category: CategoryEntity): CategoryDeleteResult
}


class CategoryRepositoryIml @Inject constructor(
    private val categoryDao: CategoryDao, private val expenseDao: ExpenseDao) : CategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    override suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)
    override suspend fun saveCategory(category: CategoryEntity): Long = categoryDao.upsertCategory(category)

    override suspend fun deleteCategory(category: CategoryEntity): CategoryDeleteResult {
        val expenseCount = categoryDao.getExpenseCountForCategory(category.id)
        return if(expenseCount > 0){
            CategoryDeleteResult.BlockedByExpenses(expenseCount)
        }
        else {
            categoryDao.deleteCategory(category)
            CategoryDeleteResult.Success
        }
    }
}