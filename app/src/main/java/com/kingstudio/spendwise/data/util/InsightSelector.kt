package com.kingstudio.spendwise.data.util

import com.kingstudio.spendwise.data.model.InsightFact
import com.kingstudio.spendwise.data.model.InsightType

object InsightSelector {
    const val MAX_ITEMS = 4
    fun select(facts: List<InsightFact>, recentTypes: Set<InsightType>): List<InsightFact> {
        val sorted = facts.sortedByDescending { it.priority }
        return (sorted.filter { it.type !in recentTypes } +
                sorted.filter { it.type in recentTypes }).take(MAX_ITEMS)
    }
}