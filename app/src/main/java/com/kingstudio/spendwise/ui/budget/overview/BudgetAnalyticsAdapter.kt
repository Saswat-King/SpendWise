package com.kingstudio.spendwise.ui.budget.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.databinding.ItemBudgetAnalyticsBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver

class BudgetAnalyticsAdapter : ListAdapter<CategorySpendShare, BudgetAnalyticsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val binding = ItemBudgetAnalyticsBinding.inflate(LayoutInflater.from(parent.context),
          parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))


    class ViewHolder(private val binding: ItemBudgetAnalyticsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySpendShare) {
            binding.tvBudgetAnalyticsCategory.text = item.categoryName

            binding.tvBudgetAnalyticsAmount.text = binding.root.context.getString(
                R.string.budget_analytics_amount, item.spentAmount, item.budgetAmount
            )

            binding.ivBudgetAnalyticsIcon.setImageResource(
                CategoryIconResolver.getIconResource(item.iconKey)
            )

            binding.ivBudgetAnalyticsIcon.setColorFilter(
                CategoryIconResolver.getIconColor(item.colorHex)
            )

            binding.cardBudgetAnalyticsIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(item.colorHex)
            )
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CategorySpendShare>() {
        override fun areItemsTheSame(oldItem: CategorySpendShare, newItem: CategorySpendShare): Boolean =
            oldItem.categoryName == newItem.categoryName

        override fun areContentsTheSame(oldItem: CategorySpendShare, newItem: CategorySpendShare): Boolean =
            oldItem == newItem
    }
}