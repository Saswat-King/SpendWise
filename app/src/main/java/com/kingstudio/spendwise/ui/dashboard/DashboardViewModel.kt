package com.kingstudio.spendwise.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.model.BudgetOverview
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.model.PeriodComparison
import com.kingstudio.spendwise.data.repository.BudgetRepository
import com.kingstudio.spendwise.data.repository.ExpenseRepository
import com.kingstudio.spendwise.data.repository.IncomeRepository
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import com.kingstudio.spendwise.data.util.GreetingProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            incomeRepository.ensureCurrentPeriodSalaryExists()
            budgetRepository.ensureCurrentPeriodBudgetsExist()
        }
    }
    private val previousPeriodKey = DateRangeCalculator.previousPeriodKey()
    private val previousPeriodRange: DateRange = DateRangeCalculator
        .epochRangeForPeriodKey(previousPeriodKey)
        .let { (start, end) -> DateRange.Custom(start,end) }

    private data class DashboardExtras(
        val previousIncome: Double,
        val previousExpenses: Double,
        val dailyTotals: List<ExpenseDao.DailyTotal>,
        val budgetOverview: BudgetOverview,
        val recentExpenses: List<ExpenseWithCategory>
    )

    private val extras: Flow<DashboardExtras> = combine(
        incomeRepository.getNormalizedIncomeForPeriod(previousPeriodKey),
        expenseRepository.getTotalExpensesForRange(previousPeriodRange),
        expenseRepository.getDailySpendingTrend(DateRange.ThisMonth),
        budgetRepository.getCurrentPeriodBudgetOverview(),
        expenseRepository.getRecentExpenses(5)
    ) { prevIncome, prevExpenses, daily, budget, recent ->
        DashboardExtras(prevIncome, prevExpenses, daily, budget, recent)
    }

    val uiState: StateFlow<DashboardScreenState> = combine(
        incomeRepository.getNormalizedCurrentMonthlyIncome(),
        incomeRepository.getIncomeTrend(),
        expenseRepository.getTotalExpensesForRange(DateRange.ThisMonth),
        expenseRepository.getExpenseTrend(DateRange.ThisMonth),
        extras
    ) { income, incomeTrend, expenses, expenseTrend, ex ->
        val balance = income - expenses
        val previousBalance = ex.previousIncome - ex.previousExpenses

        DashboardScreenState.Success(
            DashboardUiState(
                greeting = GreetingProvider.currentGreeting(),
                availableBalance = balance,
                balanceTrend = PeriodComparison.calculate(balance, previousBalance),
                income = income,
                incomeTrend = incomeTrend,
                expenses = expenses,
                expenseTrend = expenseTrend,
                spendingTrendPoints = ex.dailyTotals,
                budgetOverview = ex.budgetOverview.takeIf { it.categoryProgress.isNotEmpty() },
                recentExpenses = ex.recentExpenses
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardScreenState.Loading
    )
}