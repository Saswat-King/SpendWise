package com.kingstudio.spendwise.ui.budget.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.model.CategoryBudgetProgress
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.repository.BudgetRepository
import com.kingstudio.spendwise.data.repository.CurrencyRepository
import com.kingstudio.spendwise.data.repository.ExpenseRepository
import com.kingstudio.spendwise.data.util.BudgetPerformanceCalculator
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val BREAKDOWN_TOP_COUNT = 3
private const val OTHER_COLOR_HEX = "#90A4AE"

@HiltViewModel
class BudgetScreenViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _selectedPeriodKey = MutableStateFlow(DateRangeCalculator.currentPeriodKey())
    val selectedPeriodKey: StateFlow<String> = _selectedPeriodKey.asStateFlow()

    val currencyCode: Flow<String> = currencyRepository.selectedCurrencyCode

    val availablePeriods: StateFlow<List<String>> = budgetRepository.getAvailablePeriods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BudgetScreenState> = _selectedPeriodKey.flatMapLatest { periodKey ->
        val isCurrentPeriod = periodKey == DateRangeCalculator.currentPeriodKey()
        val (start, end) = DateRangeCalculator.epochRangeForPeriodKey(periodKey)

        combine(
            budgetRepository.getBudgetOverviewForPeriod(periodKey),
            expenseRepository.getDailySpendingTrend(DateRange.Custom(start, end)),
            availablePeriods
        ) { overview, dailyTotals, periods  ->

            if(overview.categoryProgress.isEmpty()) {
                BudgetScreenState.Empty
            }
            else {
                val spendShares = buildSpendBreakdown(overview.categoryProgress)

                val performancePoints = BudgetPerformanceCalculator.calculate(
                    periodKey = periodKey,
                    totalBudget = overview.totalBudget,
                    dailyTotals = dailyTotals,
                    isCurrentPeriod = isCurrentPeriod
                )

                val categoryRows = overview.categoryProgress.map { cp ->
                    BudgetCategoryRow(
                        categoryName = cp.category.name,
                        iconKey = cp.category.iconKey,
                        colorHex = cp.category.colorHex,
                        budgetAmount = cp.budgetAmount,
                        spentAmount = cp.spentAmount,
                        remainingAmount = cp.remainingAmount,
                        percentUsed = cp.percentUsed,
                        zone = cp.zone,
                    )
                }
                BudgetScreenState.Success(
                    BudgetScreenUiState(
                        selectedPeriodKey = periodKey,
                        availablePeriods = periods,
                        overview = overview,
                        spendShares = spendShares,
                        performancePoints = performancePoints,
                        categoryRows = categoryRows
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetScreenState.Loading
    )

    fun onPeriodSelected(periodKey: String) {
        _selectedPeriodKey.value = periodKey
    }


    private fun buildSpendBreakdown(progress: List<CategoryBudgetProgress>): List<CategorySpendShare> {
        val spending = progress.filter { it.spentAmount > 0 }.sortedByDescending { it.spentAmount }
        if(spending.isEmpty()) return emptyList()

        val top = if(spending.size <= BREAKDOWN_TOP_COUNT + 1) spending else spending.take(BREAKDOWN_TOP_COUNT)
        val rest = spending.drop(top.size)

        val shares = top.map{
            CategorySpendShare(
                it.category.name,
                it.category.iconKey,
                it.category.colorHex,
                it.spentAmount,
                0
            )
        }.toMutableList()

        if(rest.isNotEmpty()) {
            shares += CategorySpendShare(
                "Other", "ic_other",
                OTHER_COLOR_HEX, rest.sumOf { it.spentAmount },
                0, rest.size
            )
        }

        val percents = roundedPercents(shares.map { it.spentAmount })
        return shares.mapIndexed { index, share -> share.copy(percentOfTotalSpent = percents[index])}
    }


    private fun roundedPercents(values: List<Double>): List<Int> {
        val raw = values.map { it / values.sum()*100 }
        val result = raw.map { it.toInt() }.toMutableList()
        val byRemainder = raw.indices.sortedByDescending { raw[it] - result[it] }
        repeat(100 - result.sum()) { result[byRemainder[it]]++ }
        return result
    }
}