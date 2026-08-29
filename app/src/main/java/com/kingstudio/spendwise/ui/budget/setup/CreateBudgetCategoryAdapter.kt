package com.kingstudio.spendwise.ui.budget.setup

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kingstudio.spendwise.databinding.ItemCreateBudgetCategoryBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver


class CreateBudgetCategoryAdapter(
    private val onAmountChanged: (categoryId: Long, amount: String) -> Unit
) : ListAdapter<BudgetCategoryInput, CreateBudgetCategoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val binding =  ItemCreateBudgetCategoryBinding.inflate(LayoutInflater.from(
           parent.context),parent,false)

        return ViewHolder(binding, onAmountChanged)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),isLast = position == itemCount - 1)
    }

    class ViewHolder(
        private val binding: ItemCreateBudgetCategoryBinding,
        private val onAmountChanged: (categoryId: Long, amount: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentWatcher: TextWatcher? = null

        fun bind(item: BudgetCategoryInput, isLast: Boolean) {
            binding.tvBudgetCategoryName.text = item.category.name

            binding.imgBudgetCategory.setImageResource(
                CategoryIconResolver.getIconResource(item.category.iconKey)
            )

            binding.imgBudgetCategory.setColorFilter(
                CategoryIconResolver.getIconColor(item.category.colorHex)
            )

            binding.categoryIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(item.category.colorHex)
            )

            currentWatcher?.let { binding.etBudgetAmount.removeTextChangedListener(it) }

            if(binding.etBudgetAmount.text.toString() != item.amount) {
                binding.etBudgetAmount.setText(item.amount)
                binding.etBudgetAmount.setSelection(binding.etBudgetAmount.text?.length ?: 0)
            }

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onAmountChanged(item.category.id, s?.toString() ?: "")
                }
            }

            binding.etBudgetAmount.addTextChangedListener(watcher)
            currentWatcher = watcher

            binding.etBudgetAmount.imeOptions =
                if(isLast) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT

            binding.etBudgetAmount.setOnEditorActionListener { _, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    val nextPosition = bindingAdapterPosition + 1
                    val nextHolder = (binding.root.parent as? RecyclerView)
                        ?.findViewHolderForAdapterPosition(nextPosition) as? ViewHolder

                    nextHolder?.binding?.etBudgetAmount?.requestFocus() ?: false
                }
                else {
                    false
                }
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<BudgetCategoryInput>() {
        override fun areItemsTheSame(oldItem: BudgetCategoryInput, newItem: BudgetCategoryInput): Boolean =
            oldItem.category == newItem.category

        override fun areContentsTheSame(oldItem: BudgetCategoryInput, newItem: BudgetCategoryInput): Boolean =
            oldItem == newItem
    }
}