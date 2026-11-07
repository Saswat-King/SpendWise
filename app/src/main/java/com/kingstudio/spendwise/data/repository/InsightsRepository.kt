package com.kingstudio.spendwise.data.repository

import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.model.HealthScore
import com.kingstudio.spendwise.data.model.InsightFact
import com.kingstudio.spendwise.data.model.InsightType
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import com.kingstudio.spendwise.data.util.HealthScoreCalculator
import com.kingstudio.spendwise.data.util.InsightFactsGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class InsightsRepository @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val budgetRepository: BudgetRepository
) {
    data class Snapshot(val score: HealthScore?, val facts: List<InsightFact>)

    fun observeSnapshot(): Flow<Snapshot> {
        val lastMonthRange = DateRangeCalculator
            .epochRangeForPeriodKey(DateRangeCalculator.previousPeriodKey())
            .let { (start, end) -> DateRange.Custom(start, end) }

        return combine(
            incomeRepository.getNormalizedCurrentMonthlyIncome(),
            expenseRepository.getExpensesForRange(DateRange.ThisMonth),
            expenseRepository.getExpensesForRange(lastMonthRange),
            budgetRepository.getCurrentPeriodBudgetOverview()
        ) { income, thisMonth, lastMonth, budget ->
            val today = LocalDate.now()

            val score = HealthScoreCalculator.calculate(
                HealthScoreCalculator.Input(
                    monthlyIncome = income,
                    spentSoFar = thisMonth.sumOf { it.expense.amount },
                    spentSamePointLastMonth = if(lastMonth.isEmpty()) null
                      else InsightFactsGenerator.spentUpToDay(lastMonth, today.dayOfMonth),
                    categoryProgress = budget.categoryProgress,
                    expenseCount = thisMonth.size,
                    today = today
                )
            )

            val facts = InsightFactsGenerator.generate(
                InsightFactsGenerator.Input(income, thisMonth, lastMonth, today)
            )

            val tip = score?.let { InsightFact(InsightType.SAVING_TIP, 30, it.weakestFactor.name,
                0.0, 0) }

            Snapshot(score, facts + listOfNotNull(tip))
        }
    }
}