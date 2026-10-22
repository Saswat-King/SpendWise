package com.kingstudio.spendwise.ui.budget.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.util.CurrencyFormatter
import com.kingstudio.spendwise.databinding.ItemBudgetAnalyticsBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver

class BudgetAnalyticsAdapter : ListAdapter<CategorySpendShare, BudgetAnalyticsAdapter.ViewHolder>(DiffCallback) {

    var currencyCode: String = "INR"
        set(value) {
            if(field != value) {
                field = value
                notifyItemRangeChanged(0, itemCount)
            }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val binding = ItemBudgetAnalyticsBinding.inflate(LayoutInflater.from(parent.context),
          parent, false)

         return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), currencyCode)


    class ViewHolder(private val binding: ItemBudgetAnalyticsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySpendShare, currencyCode: String) {

            val context = binding.root.context
            val spent  = CurrencyFormatter.formatCompact(item.spentAmount, currencyCode)

            binding.tvBudgetAnalyticsCategory.text = item.categoryName

           binding.tvBudgetAnalyticsSpent.text = if(item.categoryCount > 1) {
               context.getString(R.string.budget_analytics_other_spent, item.categoryCount, spent)
           } else {
               context.getString(R.string.budget_analytics_spent, spent)
           }


           binding.tvBudgetAnalyticsPercentage.text = context.getString(
               R.string.budget_analytics_percentage, item.percentOfTotalSpent)

           binding.tvBudgetAnalyticsPercentage.setTextColor(CategoryIconResolver.getIconColor(item.colorHex))

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