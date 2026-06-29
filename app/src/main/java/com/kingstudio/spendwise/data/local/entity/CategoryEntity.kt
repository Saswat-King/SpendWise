package com.kingstudio.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
