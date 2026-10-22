package com.kingstudio.spendwise.ui.budget.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.model.BudgetZone
import com.kingstudio.spendwise.data.util.CurrencyFormatter
import com.kingstudio.spendwise.databinding.ItemBudgetCategoryStatusBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver


class BudgetCategoryStatusAdapter :
    ListAdapter<BudgetCategoryRow, BudgetCategoryStatusAdapter.ViewHolder>(DiffCallback) {

    var currencyCode: String = "INR"
        set(value) {
            if(field != value) {
                field = value
                notifyItemRangeChanged(0, itemCount)
            }
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBudgetCategoryStatusBinding.inflate(LayoutInflater
            .from(parent.context),parent,false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), currencyCode)

    class ViewHolder(private val binding: ItemBudgetCategoryStatusBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BudgetCategoryRow, currencyCode: String) {
            val context = binding.root.context

            binding.tvBudgetStatusCategoryName.text = item.categoryName

            binding.tvBudgetStatusSpent.text = context.getString(
                R.string.budget_status_spent_of,
                CurrencyFormatter.formatCompact(item.spentAmount, currencyCode),
                CurrencyFormatter.formatCompact(item.budgetAmount, currencyCode)
            )

            binding.tvBudgetStatusPercent.text = context.getString(R.string.budget_status_percent, item.displayPercent)
            binding.budgetStatusProgress.progress = item.displayPercent

            val (colorRes, labelRes) = when {
                item.isOver -> Pair(R.color.sw_indicator_dark_red_bg, R.string.budget_status_over)
                item.isFullyUsed -> Pair(R.color.sw_indicator_light_red_bg, R.string.budget_status_fully_used)
                item.zone == BudgetZone.SAFE -> Pair(R.color.sw_indicator_green_bg, R.string.budget_status_on_track)
                else -> Pair(R.color.sw_indicator_amber_bg, R.string.budget_status_near_limit)
            }

            val color = ContextCompat.getColor(context, colorRes)
            binding.budgetStatusProgress.setIndicatorColor(color)
            binding.tvBudgetStatusPercent.setTextColor(color)
            binding.tvBudgetStatusBadge.setTextColor(color)
            binding.tvBudgetStatusBadge.setText(labelRes)

            binding.tvBudgetStatusRemaining.text = if (item.isOver) {
                context.getString(R.string.budget_status_over_by,
                    CurrencyFormatter.formatCompact(-item.remainingAmount, currencyCode))
            } else {
                context.getString(R.string.budget_status_remaining,
                    CurrencyFormatter.formatCompact(item.remainingAmount, currencyCode))
            }


            binding.budgetStatusProgress.progress = item.percentUsed.coerceIn(0,100)

            binding.ivBudgetStatusCategoryIcon.setImageResource(
                CategoryIconResolver.getIconResource(item.iconKey)
            )

            binding.ivBudgetStatusCategoryIcon.setColorFilter(
                CategoryIconResolver.getIconColor(item.colorHex)
            )

            binding.cardBudgetStatusIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(item.colorHex)
            )
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<BudgetCategoryRow>() {
        override fun areContentsTheSame(oldItem: BudgetCategoryRow, newItem: BudgetCategoryRow): Boolean =
            oldItem.categoryName == newItem.categoryName

        override fun areItemsTheSame(oldItem: BudgetCategoryRow, newItem: BudgetCategoryRow): Boolean =
            oldItem == newItem

    }
}