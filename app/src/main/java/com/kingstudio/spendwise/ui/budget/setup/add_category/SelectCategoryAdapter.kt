package com.kingstudio.spendwise.ui.budget.setup.add_category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.databinding.ItemSelectCategoryBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver

class SelectCategoryAdapter(
    private val onCategoryClicked: (CategoryEntity) -> Unit
) : ListAdapter<SelectableCategory, SelectCategoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemSelectCategoryBinding.inflate(LayoutInflater.from(
            parent.context), parent, false)

        return ViewHolder(binding,onCategoryClicked)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemSelectCategoryBinding,
        private val onCategoryClicked: (CategoryEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SelectableCategory) {
            val category = item.category
            val context  = binding.root.context

            binding.tvCategoryName.text = category.name

            binding.ivCategoryIcon.setImageResource(
                CategoryIconResolver.getIconResource(category.iconKey)
            )

            binding.ivCategoryIcon.setColorFilter(
                CategoryIconResolver.getIconColor(category.colorHex)
            )

            binding.categoryIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(category.colorHex)
            )

            if(item.isSelected) {
                binding.categoryCard.strokeColor =
                    ContextCompat.getColor(context, R.color.category_selected_badge)

                binding.selectedBadge.visibility = View.VISIBLE
                binding.ivCheckmark.visibility = View.VISIBLE
            }
            else {
                binding.categoryCard.strokeColor =
                    ContextCompat.getColor(context, R.color.category_card_stroke)

                binding.selectedBadge.visibility = View.GONE
                binding.ivCheckmark.visibility = View.GONE
            }

            binding.root.setOnClickListener { onCategoryClicked(category) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<SelectableCategory>() {

        override fun areItemsTheSame(oldItem: SelectableCategory, newItem: SelectableCategory): Boolean =
            oldItem.category.id == newItem.category.id

        override fun areContentsTheSame(oldItem: SelectableCategory, newItem: SelectableCategory): Boolean =
            oldItem == newItem
    }
}