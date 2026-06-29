package com.kingstudio.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val source: IncomeSource,
    val amount: Double,
    val frequency: IncomeFrequency,
    val note: String ="",
    val createdAt: Long = System.currentTimeMillis()
)

enum class IncomeSource {
    SALARY,
    FREELANCE,
    BONUS,
    OTHER
}

enum class IncomeFrequency {
    MONTHLY,
    YEARLY
}
