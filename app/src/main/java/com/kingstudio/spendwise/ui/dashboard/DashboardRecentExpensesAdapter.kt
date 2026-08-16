package com.kingstudio.spendwise.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.local.relation.ExpenseWithCategory
import com.kingstudio.spendwise.data.util.RelativeDateFormatter
import com.kingstudio.spendwise.databinding.ItemDashboardRecentExpensesBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver

class DashboardRecentExpensesAdapter : ListAdapter<ExpenseWithCategory, DashboardRecentExpensesAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemDashboardRecentExpensesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder(private val binding: ItemDashboardRecentExpensesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExpenseWithCategory) {
            binding.tvExpenseCategory.text = item.category.name
            binding.tvExpenseDescription.text = item.expense.title
            binding.tvExpenseAmount.text = binding.root.context.getString(
                R.string.recent_expense_amount, item.expense.amount
            )
            binding.tvExpenseDate.text = RelativeDateFormatter.format(item.expense.date)

            binding.ivExpenseIcon.setImageResource(
                CategoryIconResolver.getIconResource(item.category.iconKey)
            )

            binding.ivExpenseIcon.setColorFilter(
                CategoryIconResolver.getIconColor(item.category.colorHex)
            )

            binding.ivExpenseIcon.setBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(item.category.colorHex)
            )
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ExpenseWithCategory>() {
        override fun areItemsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory): Boolean =
            oldItem.expense.id == newItem.expense.id

        override fun areContentsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory): Boolean =
            oldItem == newItem
    }
}