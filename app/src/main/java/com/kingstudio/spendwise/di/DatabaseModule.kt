package com.kingstudio.spendwise.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kingstudio.spendwise.data.local.SpendWiseDatabase
import com.kingstudio.spendwise.data.local.dao.BudgetDao
import com.kingstudio.spendwise.data.local.dao.CategoryDao
import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import com.kingstudio.spendwise.data.local.dao.IncomeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) : SpendWiseDatabase {

        return Room.databaseBuilder(context, SpendWiseDatabase::class.java,"spendwise.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase){
                    DefaultData.categories.forEach{ (name, iconKey, colorHEx) ->
                        db.execSQL(
                            """INSERT INTO categories (name, iconKey, colorHex, isDefault, createdAt) 
                               VALUES(?,?,?,1,?)""" ,
                            arrayOf<Any>(name,iconKey,colorHEx, System.currentTimeMillis())
                        )
                    }
                }
            }).fallbackToDestructiveMigration()
            .build()
    }


    @Provides fun provideCategoryDao(db: SpendWiseDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideExpenseDao(db: SpendWiseDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideBudgetDao(db: SpendWiseDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideIncomeDao(db: SpendWiseDatabase): IncomeDao = db.incomeDao()
}


private object DefaultData {
    val categories = listOf(
        Triple("Shopping",            "ic_shopping_bag",   "#5C6BC0"),
        Triple("Food & Dining",       "ic_food",           "#FF7043"),
        Triple("Transport",           "ic_transport",      "#26A69A"),
        Triple("Bills & Utilities",   "ic_bills",          "#EF5350"),
        Triple("Entertainment",       "ic_entertainment",  "#AB47BC"),
        Triple("Travel",              "ic_travel",         "#42A5F5"),
        Triple("Groceries",           "ic_groceries",      "#66BB6A"),
        Triple("Health",              "ic_health",         "#EC407A"),
        Triple("Education",           "ic_education",      "#FFA726"),
        Triple("Other",               "ic_other",          "#78909C")
    )
}