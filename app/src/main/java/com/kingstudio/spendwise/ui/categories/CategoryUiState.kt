package com.kingstudio.spendwise.ui.categories

import com.kingstudio.spendwise.data.local.entity.CategoryEntity

sealed class CategoryListUiState {
    object Loading: CategoryListUiState()
    data class Success(val category: List<CategoryEntity>) : CategoryListUiState()
    object Empty: CategoryListUiState()
}

data class CategoryFormState(
    val id: Long? = null,
    val name: String = "",
    val iconKey: String = CategoryOptions.icons.first(),
    val colorHex: String = CategoryOptions.colors.first(),
    val isDefault: Boolean = false,
    val nameError: String? = null,
    val isSaving: Boolean = false
)

sealed class CategoryUiEvent{
    object CategorySaved : CategoryUiEvent()
    object CategoryDeleted : CategoryUiEvent()
    data class DeleteBlocked(val expenseCount: Int) : CategoryUiEvent()
}