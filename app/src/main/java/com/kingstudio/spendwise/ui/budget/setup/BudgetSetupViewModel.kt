package com.kingstudio.spendwise.ui.budget.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.data.local.entity.IncomeFrequency
import com.kingstudio.spendwise.data.repository.BudgetRepository
import com.kingstudio.spendwise.data.repository.CategoryRepository
import com.kingstudio.spendwise.data.repository.IncomeRepository
import com.kingstudio.spendwise.ui.common.FormMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetSetupViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val incomeRepository: IncomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mode: FormMode =
        FormMode.valueOf(savedStateHandle.get<String>("formMode") ?: FormMode.SETUP.name )

    private val _formState = MutableStateFlow(BudgetSetupFormState(mode = mode))
    val formState: StateFlow<BudgetSetupFormState> = _formState.asStateFlow()

    val summary: StateFlow<BudgetSetupSummary> = _formState
        .map { state ->
            BudgetSetupSummary(
                totalBudget = state.categoryInputs.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BudgetSetupSummary(0.0)
        )

    private val _events = Channel<BudgetSetupUiEvent>()
    val events: Flow<BudgetSetupUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {

            val incomeEntries = incomeRepository.getCurrentPeriodIncomes().first()
            val frequencyLabel = when(incomeEntries.firstOrNull()?.frequency) {
                IncomeFrequency.YEARLY -> "Yearly"
                else -> "Monthly"
            }

            val screenTitle = if(mode == FormMode.UPDATE) {
                "Manage Your $frequencyLabel Budget"
            }
            else {
                "Create Your $frequencyLabel Budget"
            }

            _formState.update { state ->
                state.copy(
                    screenTitle = screenTitle,
                    buttonLabel = if(mode == FormMode.UPDATE) "Save Changes" else "Continue"

                )
            }

            val allCategories = categoryRepository.getAllCategories().first()

            val relevantCategories = if(mode == FormMode.UPDATE) {
                val currentBudgets = budgetRepository.getCurrentPeriodBudgetOverview().first()
                val budgetedIds = currentBudgets.categoryProgress.map { it.category.id }.toSet()
                allCategories.filter { it.id in budgetedIds }
            }
            else {
                allCategories.filter { it.name in EssentialBudgetCategories.names }
            }

            val existingAmounts = if(mode == FormMode.UPDATE) {
                budgetRepository.getCurrentPeriodBudgetOverview().first()
                    .categoryProgress.associate{ it.category.id to it.budgetAmount }
            } else emptyMap()

            _formState.update {
                it.copy(
                    categoryInputs = relevantCategories.map { category ->
                        BudgetCategoryInput(
                            category = category,
                            amount = existingAmounts[category.id]?.toString() ?: ""
                        )
                    }
                )
            }
        }
    }

    fun onAmountChanged(categoryId: Long, amount: String) {
        _formState.update { state ->
            state.copy(
                categoryInputs = state.categoryInputs.map { input ->
                    if(input.category.id == categoryId) input.copy(amount = amount) else input
                }
            )
        }
    }

    fun onAddCategoryClicked() {
        viewModelScope.launch {
            _events.send(BudgetSetupUiEvent.NavigateToAddCategory)
        }
    }

    fun onCategoryAdded(category: CategoryEntity, amount: String) {
        _formState.update { state ->
            if(state.categoryInputs.any{ it.category.id == category.id }) return@update state
            state.copy(categoryInputs = state.categoryInputs + BudgetCategoryInput(category, amount))
        }
    }

    fun saveBudget() {
        val current = _formState.value
        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }

            current.categoryInputs.forEach { input ->
                val amount = input.amount.toDoubleOrNull()
                if(amount != null && amount > 0.0) {
                    budgetRepository.setBudget(input.category.id, amount)
                }
                else if (current.mode == FormMode.UPDATE) {
                    budgetRepository.getBudgetByCategory(input.category.id)?.let {
                        budgetRepository.deleteBudget(it)
                    }
                }
            }
            _formState.update { it.copy(isSaving = false) }
            _events.send(BudgetSetupUiEvent.BudgetSaved)
        }
    }
}