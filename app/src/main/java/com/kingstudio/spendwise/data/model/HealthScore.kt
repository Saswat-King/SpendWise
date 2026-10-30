package com.kingstudio.spendwise.data.model

enum class HealthStatus { GOOD, AVERAGE, NEEDS_IMPROVEMENT }
enum class HealthFactor { SAVINGS, BUDGET, TREND }

data class HealthScore(
    val score: Int,
    val status: HealthStatus,
    val weakestFactor: HealthFactor
)
