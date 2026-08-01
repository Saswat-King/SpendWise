package com.kingstudio.spendwise.ui.income

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.entity.IncomeEntity
import com.kingstudio.spendwise.data.local.entity.IncomeFrequency
import com.kingstudio.spendwise.data.local.entity.IncomeSource
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
class IncomeSetupViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mode: FormMode = FormMode.valueOf(savedStateHandle.get<String>("formMode") ?: FormMode.SETUP.name)
    private val _formState = MutableStateFlow(
        IncomeFormState(
            mode = mode,
            screenTitle = if (mode == FormMode.UPDATE) "Update Income" else "Income Setup",
            subTitle = if (mode == FormMode.UPDATE)
                   "Update your income sources" else "Let's understand your income sources",

            buttonLabel = if (mode == FormMode.UPDATE) "Save Changes" else "Continue"
        )
    )
    val formState: StateFlow<IncomeFormState> = _formState.asStateFlow()

    val summary: StateFlow<IncomeSummary> = _formState
        .map { state ->
            val base = state.primaryIncomeAmount.toDoubleOrNull() ?: 0.0
            val additional = (state.bonusAmount.toDoubleOrNull() ?: 0.0) +
                    (state.freelanceAmount.toDoubleOrNull() ?: 0.0) +
                    (state.otherAmount.toDoubleOrNull() ?: 0.0)

            IncomeSummary(
                baseIncome = base,
                additionalIncome = additional,
                totalIncome = base + additional,
                periodLabel = if (state.frequency == IncomeFrequency.MONTHLY) "Monthly" else "Yearly"
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IncomeSummary(0.0,0.0,0.0,"Monthly")
        )

    private val _events = Channel<IncomeUiEvent>()
    val event: Flow<IncomeUiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            incomeRepository.ensureCurrentPeriodSalaryExists()

            if (mode == FormMode.UPDATE) {
                val existing = incomeRepository.getCurrentPeriodIncomes().first()
                if (existing.isNotEmpty()) {
                    val frequency = existing.first().frequency
                    _formState.update { current ->
                        current.copy(
                            frequency = frequency,
                            primaryIncomeAmount = existing
                                .find { it.source == IncomeSource.SALARY }?.amount?.toString() ?: "",
                            bonusAmount = existing
                                .find { it.source == IncomeSource.BONUS }?.amount?.toString() ?: "",
                            freelanceAmount = existing
                                .find { it.source == IncomeSource.FREELANCE }?.amount?.toString() ?: "",
                            otherAmount = existing
                                .find { it.source == IncomeSource.OTHER }?.amount?.toString() ?: ""
                        )
                    }
                }
            }
        }
    }

    fun onFrequencySelected(frequency: IncomeFrequency) = _formState.update { it.copy(frequency = frequency) }
    fun onPrimaryIncomeChanged(amount: String) = _formState.update { it.copy(primaryIncomeAmount = amount, primaryIncomeError = null) }
    fun onBonusChanged(amount: String) = _formState.update { it.copy(bonusAmount = amount) }
    fun onFreelanceChanged(amount: String) = _formState.update { it.copy(freelanceAmount = amount) }
    fun onOtherChanged(amount: String) = _formState.update { it.copy(otherAmount = amount) }


    fun saveIncome() {
        val current = _formState.value
        val primaryValue = current.primaryIncomeAmount.toDoubleOrNull()

        if(primaryValue == null || primaryValue <= 0.0) {
            _formState.update { it.copy(primaryIncomeError = "Enter your primary income") }
            return
        }

        if(primaryValue < 10) {
            viewModelScope.launch {
                _events.send(IncomeUiEvent.LowIncomeWarning)
            }
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }

            val entries = buildList {
                add(IncomeEntity(source = IncomeSource.SALARY, amount = primaryValue, frequency = current.frequency))
                current.bonusAmount.toDoubleOrNull()?.takeIf { it > 0 }?.let {
                    add(IncomeEntity(source = IncomeSource.BONUS, amount = it, frequency = current.frequency))
                }
                current.freelanceAmount.toDoubleOrNull()?.takeIf { it > 0 }?.let {
                    add(IncomeEntity(source = IncomeSource.FREELANCE, amount = it, frequency = current.frequency))
                }
                current.otherAmount.toDoubleOrNull()?.takeIf { it > 0 }?.let {
                    add(IncomeEntity(source = IncomeSource.OTHER, amount = it, frequency = current.frequency ))
                }
            }

            incomeRepository.replaceCurrentPeriodIncomes(entries)
            _formState.update { it.copy(isSaving = false) }
            _events.send(IncomeUiEvent.IncomeSaved)
        }
    }
}