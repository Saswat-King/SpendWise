package com.kingstudio.spendwise.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.kingstudio.spendwise.data.local.entity.BudgetEntity
import com.kingstudio.spendwise.data.local.entity.CategoryEntity

data class BudgetWithCategory(
    @Embedded val budget: BudgetEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity
)
