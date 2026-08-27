package com.kingstudio.spendwise.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.model.BudgetZone
import com.kingstudio.spendwise.data.model.CategoryBudgetProgress
import com.kingstudio.spendwise.databinding.ItemDashboardBudgetCategoriesBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver

class DashboardBudgetCategoriesAdapter : ListAdapter<CategoryBudgetProgress, DashboardBudgetCategoriesAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemDashboardBudgetCategoriesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemDashboardBudgetCategoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryBudgetProgress) {
            binding.tvBudgetCategoryName.text = item.category.name

            binding.tvBudgetCategoryPercent.text =  binding.root.context.getString(
                R.string.budget_category_percent, item.percentUsed)

            binding.budgetCategoryProgress.progress = item.percentUsed.coerceIn(0,100)

            binding.tvBudgetCategoryAmount.text =
                binding.root.context.getString(
                    R.string.budget_category_amount, item.spentAmount, item.budgetAmount)

            val color = when(item.zone) {
                BudgetZone.SAFE -> "#4CAF50".toColorInt()
                BudgetZone.CAUTION -> "#FFA726".toColorInt()
                BudgetZone.DANGER -> "#EF530".toColorInt()
            }

            binding.budgetCategoryProgress.setIndicatorColor(color)

            binding.ivBudgetCategoryIcon.setImageResource(
                CategoryIconResolver.getIconResource(item.category.iconKey)
            )

            binding.ivBudgetCategoryIcon.setColorFilter(
                CategoryIconResolver.getIconColor(item.category.colorHex)
            )

            binding.cardBudgetCategoryIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(item.category.colorHex)
            )
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CategoryBudgetProgress>() {
        override fun areItemsTheSame(oldItem: CategoryBudgetProgress, newItem: CategoryBudgetProgress): Boolean =
            oldItem.category.id == newItem.category.id

        override fun areContentsTheSame(oldItem: CategoryBudgetProgress, newItem: CategoryBudgetProgress): Boolean =
            oldItem == newItem
    }
}