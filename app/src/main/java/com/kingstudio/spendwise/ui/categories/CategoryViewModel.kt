package com.kingstudio.spendwise.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.data.model.CategoryDeleteResult
import com.kingstudio.spendwise.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository) : ViewModel() {

        val listUiState: StateFlow<CategoryListUiState> = categoryRepository
            .getAllCategories()
            .map { categories ->
                if(categories.isEmpty()) CategoryListUiState.Empty
                else CategoryListUiState.Success(categories.sortedBy { it.name })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CategoryListUiState.Loading
            )

    private val _formState = MutableStateFlow(CategoryFormState())
    val formState: StateFlow<CategoryFormState> = _formState.asStateFlow()

    private val _events = Channel<CategoryUiEvent>()
    val event: Flow<CategoryUiEvent> = _events.receiveAsFlow()


    fun startAdd() {
        _formState.value = CategoryFormState()
    }

    fun startEdit(category: CategoryEntity) {
        _formState.value = CategoryFormState(
            id = category.id,
            name = category.name,
            iconKey = category.iconKey,
            colorHex = category.colorHex,
            isDefault = category.isDefault
        )
    }

    fun onNamedChanged(name: String) {
        _formState.update { it.copy(name = name, nameError = null) }
    }

    fun onIconSelected(iconKey: String) {
        _formState.update { it.copy(iconKey = iconKey) }
    }

    fun onColorSelected(colorHex: String) {
        _formState.update { it.copy(colorHex = colorHex) }
    }

    fun saveCategory() {
        val current = _formState.value
        val trimmedName = current.name.trim()

        if(trimmedName.isEmpty()){
            _formState.update { it.copy(nameError = "Category name is required") }
            return
        }

        val isDuplicate = (listUiState.value as? CategoryListUiState.Success)
            ?.category
            ?.any{it.name.equals(trimmedName, ignoreCase = true) && it.id != current.id}
            ?: false

        if(isDuplicate) {
            _formState.update { it.copy(nameError = "A category with this name already exists") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }

            if(current.id == null) {
                categoryRepository.saveCategory(
                    CategoryEntity(
                        name = trimmedName,
                        iconKey = current.iconKey,
                        colorHex = current.colorHex,
                        isDefault = false
                    )
                )
            }
            else {
                categoryRepository.saveCategory(
                    CategoryEntity(
                        id = current.id,
                        name = trimmedName,
                        iconKey = current.iconKey,
                        colorHex = current.colorHex,
                        isDefault = current.isDefault
                    )
                )
            }
            _formState.update { it.copy(isSaving = false) }
            _events.send(CategoryUiEvent.CategorySaved)
        }
    }


    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            when (val result = categoryRepository.deleteCategory(category)) {
                is CategoryDeleteResult.Success -> _events.send(CategoryUiEvent.CategoryDeleted)
                is CategoryDeleteResult.BlockedByExpenses -> _events.send(CategoryUiEvent.DeleteBlocked(result.expenseCount))
            }
        }
    }
}