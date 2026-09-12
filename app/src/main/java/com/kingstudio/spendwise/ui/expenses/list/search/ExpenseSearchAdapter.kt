package com.kingstudio.spendwise.ui.expenses.list.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.local.entity.ExpenseEntity
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.util.RelativeDateFormatter
import com.kingstudio.spendwise.databinding.ItemExpenseRowBinding
import com.kingstudio.spendwise.ui.budget.setup.CreateBudgetCategoryAdapter
import com.kingstudio.spendwise.ui.common.CategoryIconResolver
import com.kingstudio.spendwise.ui.expenses.list.ExpenseGroupAdapter.GroupViewHolder

class ExpenseSearchAdapter(
    private val onRowClicked: (ExpenseWithCategory) -> Unit
) : ListAdapter<ExpenseWithCategory, ExpenseSearchAdapter.ViewHolder>(DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onRowClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemExpenseRowBinding,
        private val onRowClicked: (ExpenseWithCategory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExpenseWithCategory) {
            val expense = item.expense
            val category = item.category

            binding.textCategory.text = category.name
            binding.textDescription.text = expense.title
            binding.textMetadata.text = formatMetadata(expense)
            binding.textAmount.text = itemView.context.getString(
                R.string.search_expense_amount, expense.amount
            )

            binding.imageCategoryIcon.setImageResource(CategoryIconResolver.getIconResource(category.iconKey))
            binding.imageCategoryIcon.setColorFilter(CategoryIconResolver.getIconColor(category.colorHex))

            binding.categoryIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(category.colorHex)
            )

            binding.root.setOnClickListener { onRowClicked(item) }
        }

        private fun formatMetadata(expense: ExpenseEntity): String {
            val dateLabel = RelativeDateFormatter.format(expense.date)
            val time = RelativeDateFormatter.formatTime(expense.date)
            val notePart = if(!expense.note.isNullOrBlank()) " • ${expense.note}" else ""
            return "$dateLabel • $time$notePart "
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ExpenseWithCategory>() {

        override fun areItemsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory): Boolean =
            oldItem.expense.id == newItem.expense.id

        override fun areContentsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory): Boolean =
            oldItem == newItem
    }
}