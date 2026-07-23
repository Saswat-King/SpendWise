package com.kingstudio.spendwise.ui.expenses.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.model.DateRange
import com.kingstudio.spendwise.data.model.PeriodComparison
import com.kingstudio.spendwise.data.repository.ExpenseRepository
import com.kingstudio.spendwise.data.util.RelativeDateFormatter
import com.kingstudio.spendwise.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    private val _dateRange = MutableStateFlow<DateRange>(DateRange.ThisMonth)
    val dateRange: StateFlow<DateRange> = _dateRange.asStateFlow()

    private val _pendingDeletions = MutableStateFlow<Map<Long, Job>>(emptyMap())

    private val _events = Channel<ExpenseUiEvent>()
    val events: Flow<ExpenseUiEvent> = _events.receiveAsFlow()

    private data class RawExpenseData(
        val expenses: List<ExpenseWithCategory>,
        val total: Double,
        val trend: PeriodComparison,
        val highest: ExpenseWithCategory?,
        val lowest: ExpenseWithCategory?
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawData: Flow<RawExpenseData> = _dateRange.flatMapLatest { range ->
        combine(
            expenseRepository.getExpensesForRange(range),
            expenseRepository.getTotalExpensesForRange(range),
            expenseRepository.getExpenseTrend(range),
            expenseRepository.getHighestExpense(range),
            expenseRepository.getLowestExpense(range)
        ) { expenses, total, trend, highest, lowest ->
            RawExpenseData(expenses, total, trend, highest, lowest)
        }
    }

    val uiState: StateFlow<ExpenseListUiState> = combine(
        rawData, _pendingDeletions
    ) { raw, pending ->

        val visible = raw.expenses.filterNot { pending.containsKey(it.expense.id) }

        if (visible.isEmpty()) {
            ExpenseListUiState.Empty
        } else {
            ExpenseListUiState.Success(
                summary = ExpensesSummary(raw.total, raw.trend, raw.highest, raw.lowest),
                groupedItems = visible.groupByDate()

            )
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseListUiState.Loading
    )

    fun onDateRangeSelected(range: DateRange) {
        _dateRange.value = range
    }

    private val undoWindowMillis = 4_000L

    fun onDeleteExpense(item: ExpenseWithCategory) {
        val expenses = item.expense

        if(_pendingDeletions.value.containsKey(expenses.id)) return

        val job = applicationScope.launch {
            delay(undoWindowMillis.milliseconds)
            expenseRepository.deleteExpense(expenses)
            _pendingDeletions.update { it - expenses.id }
        }
        _pendingDeletions.update { it + (expenses.id to job) }

        viewModelScope.launch {
            _events.send(ExpenseUiEvent.ShowUndoDelete(expenses.id, expenses.title))
        }
    }

    fun onUndoDelete(expenseId: Long) {
        _pendingDeletions.value[expenseId]?.cancel()
        _pendingDeletions.update { it - expenseId }

    }

    private fun List<ExpenseWithCategory>.groupByDate(): List<ExpenseListItem> {
        return this
            .groupBy { RelativeDateFormatter.toLocalDate(it.expense.date) }
            .toSortedMap(compareByDescending { it })
            .flatMap { (date, dayExpenses) ->
                val sorted = dayExpenses.sortedByDescending { it.expense.date }
                val dayTotal = sorted.sumOf { it.expense.amount }
                listOf(
                    ExpenseListItem.DateHeader(RelativeDateFormatter.format(sorted.first().expense.date),dayTotal)
                ) + sorted.map { ExpenseListItem.Row(it) }
            }

    }
}