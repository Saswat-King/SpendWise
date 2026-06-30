package com.kingstudio.spendwise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kingstudio.spendwise.data.Converters
import com.kingstudio.spendwise.data.local.dao.BudgetDao
import com.kingstudio.spendwise.data.local.dao.CategoryDao
import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import com.kingstudio.spendwise.data.local.dao.IncomeDao
import com.kingstudio.spendwise.data.local.entity.BudgetEntity
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.data.local.entity.ExpenseEntity
import com.kingstudio.spendwise.data.local.entity.IncomeEntity

@Database(entities = [
    CategoryEntity::class,
    ExpenseEntity::class,
    BudgetEntity::class,
    IncomeEntity::class
], version = 1, exportSchema = true)

@TypeConverters(Converters::class)
abstract class SpendWiseDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun incomeDao(): IncomeDao
}