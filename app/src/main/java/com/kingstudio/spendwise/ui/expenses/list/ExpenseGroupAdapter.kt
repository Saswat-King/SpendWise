package com.kingstudio.spendwise.ui.expenses.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.local.entity.ExpenseEntity
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.util.RelativeDateFormatter
import com.kingstudio.spendwise.databinding.ItemExpenseGroupBinding
import com.kingstudio.spendwise.databinding.ItemExpenseRowBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver

class ExpenseGroupAdapter(
    private val onRowClicked: (ExpenseWithCategory) -> Unit,
    private val onRowLongPressed: (ExpenseWithCategory) -> Unit
) : ListAdapter<ExpenseGroup, ExpenseGroupAdapter.GroupViewHolder>(DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseGroupAdapter.GroupViewHolder {
        val binding = ItemExpenseGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GroupViewHolder(binding, onRowClicked, onRowLongPressed)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GroupViewHolder(
        private val binding: ItemExpenseGroupBinding,
        private val onRowClicked: (ExpenseWithCategory) -> Unit,
        private val onRowLongPressed: (ExpenseWithCategory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: ExpenseGroup) {
            binding.textDate.text = group.dateLabel
            binding.textGroupTotal.text = itemView.context.getString(
                R.string.expense_group_total,
                group.totalAmount
            )

            binding.layoutExpenses.removeAllViews()

            val inflater = LayoutInflater.from(binding.root.context)
            group.expenses.forEach { item ->
                val rowBinding = ItemExpenseRowBinding.inflate(inflater, binding.layoutExpenses,false)
                bindRow(rowBinding, item)
                binding.layoutExpenses.addView(rowBinding.root)
            }
        }

        private fun bindRow(rowBinding: ItemExpenseRowBinding, item: ExpenseWithCategory) {
            val expense = item.expense
            val category = item.category

            rowBinding.textCategory.text = category.name
            rowBinding.textDescription.text = expense.title

            rowBinding.textMetadata.text = itemView.context.getString(
                R.string.expense_metadata, expense.amount
            )

            rowBinding.imageCategoryIcon.setImageResource(
                CategoryIconResolver.getIconResource(category.iconKey)
            )

            rowBinding.imageCategoryIcon.setColorFilter(
                CategoryIconResolver.getIconColor(category.colorHex)
            )

            rowBinding.categoryIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(category.colorHex)
            )

            rowBinding.root.setOnClickListener { onRowClicked(item) }

            rowBinding.root.setOnLongClickListener {
                onRowLongPressed(item)
                true
            }
        }

        private fun formatMetadata(expense: ExpenseEntity): String {
            val time = RelativeDateFormatter.formatTime(expense.date)
            return if(!expense.note.isNullOrBlank()) "$time • ${expense.note}" else time
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ExpenseGroup>() {
        override fun areItemsTheSame(oldItem: ExpenseGroup, newItem: ExpenseGroup): Boolean =
            oldItem.dateLabel == newItem.dateLabel

        override fun areContentsTheSame(oldItem: ExpenseGroup, newItem: ExpenseGroup): Boolean =
            oldItem == newItem
    }
}