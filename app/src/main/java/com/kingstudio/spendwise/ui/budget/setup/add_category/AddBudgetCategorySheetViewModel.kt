package com.kingstudio.spendwise.ui.budget.setup.add_category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SelectableCategory(val category: CategoryEntity, val isSelected: Boolean)

@HiltViewModel
class AddBudgetCategorySheetViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _excludedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val availableCategories: StateFlow<List<CategoryEntity>> = combine(
        categoryRepository.getAllCategories(),
        _excludedCategoryIds
    ) { all, excluded -> all.filterNot { it.id in excluded } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),emptyList())


    val displayItems: StateFlow<List<SelectableCategory>> = combine(
        availableCategories, _selectedCategoryId
    ) { categories, selectedId ->
        categories.map{ SelectableCategory(it, isSelected = it.id == selectedId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setExcludedCategoryIds(ids: Set<Long>) {
        _excludedCategoryIds.value = ids
    }

    fun onCategorySelected(categoryId: Long) {
        _selectedCategoryId.value = categoryId
    }
}