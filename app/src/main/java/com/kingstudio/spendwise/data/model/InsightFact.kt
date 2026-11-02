package com.kingstudio.spendwise.data.model

enum class InsightType {
    PROJECTION, CATEGORY_SHIFT, WEEKEND_PATTERN,
    UNUSUAL_EXPENSE, NO_SPEND_DAYS, SMALL_SPENDS
}

data class InsightFact(
    val type: InsightType,
    val priority: Int,
    val label: String,
    val amount: Double,
    val percent: Int
)
