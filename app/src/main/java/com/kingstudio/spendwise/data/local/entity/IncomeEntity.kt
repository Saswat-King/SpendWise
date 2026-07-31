package com.kingstudio.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "incomes",
    indices = [
        Index(value = ["periodKey"]),
        Index(value = ["source","periodKey"], unique = true)
    ]
)
data class IncomeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val source: IncomeSource,
    val amount: Double,
    val frequency: IncomeFrequency,
    val periodKey: String = "",
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
