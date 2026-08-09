package com.kingstudio.spendwise.ui.budget.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.repository.BudgetRepository
import com.kingstudio.spendwise.data.repository.ExpenseRepository
import com.kingstudio.spendwise.data.util.BudgetPerformanceCalculator
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class BudgetScreenViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _selectedPeriodKey = MutableStateFlow(DateRangeCalculator.currentPeriodKey())
    val selectedPeriodKey: StateFlow<String> = _selectedPeriodKey.asStateFlow()

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
                val spendShares = overview.categoryProgress.map { cp ->
                    CategorySpendShare(
                        categoryName = cp.category.name,
                        colorHex = cp.category.colorHex,
                        spentAmount = cp.spentAmount,
                        percentOfTotalSpent = if(overview.totalSpent > 0)
                                ((cp.spentAmount / overview.totalSpent) * 100).roundToInt()
                        else 0
                    )
                }
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
                        budgetAmount = cp.budgetAmount,
                        spentAmount = cp.spentAmount,
                        remainingAmount = cp.remainingAmount,
                        percentUsed = cp.percentUsed,
                        zone = cp.zone,
                        statusLabel = cp.zone.toStatusLabel()
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
}