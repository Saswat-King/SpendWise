package com.kingstudio.spendwise.di

import com.kingstudio.spendwise.data.repository.BudgetRepository
import com.kingstudio.spendwise.data.repository.BudgetRepositoryImpl
import com.kingstudio.spendwise.data.repository.CategoryRepository
import com.kingstudio.spendwise.data.repository.CategoryRepositoryImpl
import com.kingstudio.spendwise.data.repository.ExpenseRepository
import com.kingstudio.spendwise.data.repository.ExpenseRepositoryImpl
import com.kingstudio.spendwise.data.repository.IncomeRepository
import com.kingstudio.spendwise.data.repository.IncomeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository
}
