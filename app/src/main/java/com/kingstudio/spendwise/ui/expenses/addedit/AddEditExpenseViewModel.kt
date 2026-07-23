package com.kingstudio.spendwise.ui.expenses.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.data.local.entity.ExpenseEntity
import com.kingstudio.spendwise.data.repository.CategoryRepository
import com.kingstudio.spendwise.data.repository.ExpenseRepository
import com.kingstudio.spendwise.ui.expenses.list.ExpenseUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: Long? = savedStateHandle.get<Long>("expenseId")?.takeIf { it > 0 }
    private val isEditMode = expenseId != null

    private val _formState = MutableStateFlow(
        ExpenseFormState(
            mode = if (isEditMode) ExpenseFormMode.EDIT else ExpenseFormMode.ADD,
            screenTitle = if (isEditMode) "Edit Expense" else "Add Expense",
            buttonLabel = if (isEditMode) "Save Changes" else "Save Expense"
        )
    )

    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository
        .getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),emptyList())


    private val _events = Channel<ExpenseUiEvent>()
    val events: Flow<ExpenseUiEvent> = _events.receiveAsFlow()

    init {
        if(isEditMode) {
            viewModelScope.launch {
                expenseRepository.getExpenseById(expenseId!!)?.let { expense ->
                    _formState.update {
                        it.copy(
                            expenseId = expense.id,
                            title = expense.title,
                            amount = expense.amount.toString(),
                            categoryId =  expense.categoryId,
                            date = expense.date,
                            note = expense.note ?: ""
                        )
                    }
                }
            }
        }
    }

    fun onTitleChanged(title: String) = _formState.update { it.copy(title = title.take(40), titleError = null) }
    fun onAmountChanged(amount: String) = _formState.update { it.copy(amount = amount.take(9), amountError = null) }
    fun onCategorySelected(categoryId: Long) = _formState.update { it.copy(categoryId = categoryId, categoryError = null) }
    fun onDateSelected(epochMillis: Long) =  _formState.update { it.copy(date = epochMillis) }
    fun onNoteChanged(note: String) = _formState.update { it.copy(note = note.take(150)) }


    fun saveExpense() {
        val current = _formState.value
        val amountValue = current.amount.toDoubleOrNull()
        var hasError = false

        if(current.title.isBlank()) {
            _formState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        if(amountValue == null || amountValue <= 0.0) {
            _formState.update { it.copy(amountError = "Enter a valid amount") }
            hasError = true
        }
        if(current.categoryId == null) {
            _formState.update { it.copy(categoryError = "Select a category") }
            hasError = true
        }
        if(hasError) return


        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }

            val expense = ExpenseEntity(
                id = current.expenseId ?: 0,
                title = current.title.trim(),
                amount = amountValue!!,
                categoryId = current.categoryId!!,
                date = current.date,
                note = current.note.trim().ifBlank { null }
            )
            expenseRepository.saveExpense(expense)

            _formState.update { it.copy(isSaving = false) }
            _events.send(ExpenseUiEvent.ExpenseSaved)
        }
    }
}