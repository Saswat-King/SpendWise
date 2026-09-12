package com.kingstudio.spendwise.ui.expenses.list.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.repository.ExpenseRepository
import com.kingstudio.spendwise.data.util.RelativeDateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


sealed class ExpenseSearchUiState {
    object Idle : ExpenseSearchUiState()
    object NoResults : ExpenseSearchUiState()
    data class Results(val expenses: List<ExpenseWithCategory>) : ExpenseSearchUiState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ExpenseSearchViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExpenseSearchUiState> = _query
        .debounce(200.milliseconds)
        .flatMapLatest { q ->
            if (q.isBlank()) {
                flowOf(ExpenseSearchUiState.Idle)
            }
            else {
                expenseRepository.getAllExpenses().map { all ->
                    val matches = all.filter { it.matchesSearch(q) }
                    if(matches.isEmpty()) ExpenseSearchUiState.NoResults
                    else ExpenseSearchUiState.Results(matches)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            ExpenseSearchUiState.Idle)


    fun onQueryChanged(query: String) {
        _query.value = query
    }


    private fun ExpenseWithCategory.matchesSearch(query: String): Boolean {
        val q = query.trim().lowercase()
        return expense.title.lowercase().contains(q) ||
                category.name.lowercase().contains(q) ||
                (expense.note?.lowercase()?.contains(q) == true) ||
                expense.amount.toString().contains(q) ||
                RelativeDateFormatter.format(expense.date).lowercase().contains(q)
    }

}