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
            binding.tvBudgetStatusBudget.text = CurrencyFormatter.formatCompact(item.budgetAmount, currencyCode)
            binding.tvBudgetStatusSpent.text = CurrencyFormatter.formatCompact(item.spentAmount, currencyCode)
            binding.tvBudgetStatusRemaining.text = CurrencyFormatter.formatCompact(item.remainingAmount, currencyCode)

            binding.budgetStatusProgress.progress = item.percentUsed.coerceIn(0,100)
            binding.tvBudgetStatusBadge.text = item.statusLabel

            binding.ivBudgetStatusCategoryIcon.setImageResource(
                CategoryIconResolver.getIconResource(item.iconKey)
            )

            binding.ivBudgetStatusCategoryIcon.setColorFilter(
                CategoryIconResolver.getIconColor(item.colorHex)
            )

            binding.cardBudgetStatusIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(item.colorHex)
            )

            val (indicatorColorRes, badgeColorRes) = when (item.zone) {
                BudgetZone.SAFE -> R.color.sw_indicator_green_bg to R.color.sw_badge_bg
                BudgetZone.CAUTION -> R.color.sw_indicator_amber_bg to R.color.sw_indicator_amber_bg
                BudgetZone.DANGER -> R.color.sw_indicator_red_bg to R.color.sw_indicator_red_bg
            }

            binding.budgetStatusProgress.setIndicatorColor(ContextCompat.getColor(context, indicatorColorRes))
            binding.cardBudgetStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, badgeColorRes))

        }
    }

    object DiffCallback : DiffUtil.ItemCallback<BudgetCategoryRow>() {
        override fun areContentsTheSame(oldItem: BudgetCategoryRow, newItem: BudgetCategoryRow): Boolean =
            oldItem.categoryName == newItem.categoryName

        override fun areItemsTheSame(oldItem: BudgetCategoryRow, newItem: BudgetCategoryRow): Boolean =
            oldItem == newItem

    }
}