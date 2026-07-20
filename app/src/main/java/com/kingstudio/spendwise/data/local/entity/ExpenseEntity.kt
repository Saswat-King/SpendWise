package com.kingstudio.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expenses",

foreignKeys = [
    ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT
    )
], indices = [
        Index(value = ["categoryId"]),
        Index(value = ["date"])
        ]
)



data class ExpenseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val amount: Double,
    val categoryId: Long,
    val date: Long,
    val receiptImagePath: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
