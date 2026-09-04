package com.kingstudio.spendwise.ui.expenses.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.databinding.FragmentExpensesBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExpensesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExpensesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseListViewModel by viewModels()
    private lateinit var groupAdapter: ExpenseGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }


    private fun setupRecyclerView() {
        groupAdapter = ExpenseGroupAdapter(
            onRowClicked = { expenseWithCategory -> // Todo nav graph
            },

            onRowLongPressed = { expenseWithCategory ->
                viewModel.onDeleteExpense(expenseWithCategory)
            }
        )
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { renderState(it) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }


    private fun renderState(state: ExpenseListUiState) {
        when(state) {
            is ExpenseListUiState.Loading -> {
                // future update for loading
            }

            is ExpenseListUiState.Empty -> {
                binding.tvTotalExpenseAmount.text = getString(R.string.empty_expense_total)
                binding.tvHighestExpenseAmount.text = getString(R.string.expense_no_data)
                binding.tvLowestExpenseAmount.text = getString(R.string.expense_no_data)
                groupAdapter.submitList(emptyList())
            }

            is ExpenseListUiState.Success -> {
                renderSummary(state.summary)
                groupAdapter.submitList(state.groups)
            }
        }
    }


    private fun renderSummary(summary: ExpensesSummary) {
        binding.tvTotalExpenseAmount.text =
            getString(R.string.expense_total_amount, summary.totalAmount)

        val highest = summary.highestExpense
        if(highest != null) {
            binding.tvHighestExpenseAmount.text =
                getString(R.string.expense_highest_amount, highest.expense.amount)

            binding.ivHighestExpenseIcon.setImageResource(
                CategoryIconResolver.getIconResource(highest.category.iconKey)
            )
        }
        else {
            binding.tvHighestExpenseAmount.text = getString(R.string.expense_no_data)
        }


        val lowest = summary.lowestExpense
        if(lowest != null) {
            binding.tvLowestExpenseAmount.text =
                getString(R.string.expense_lowest_amount, lowest.expense.amount)

            binding.ivLowestExpenseIcon.setImageResource(
                CategoryIconResolver.getIconResource(lowest.category.iconKey)
            )
        }
        else {
            binding.tvLowestExpenseAmount.text = getString(R.string.expense_no_data)
        }
    }
    

    private fun handleEvent(event: ExpenseUiEvent) {
        when(event) {
            is ExpenseUiEvent.ShowUndoDelete -> {
                Snackbar.make(binding.root, "Expense deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") { viewModel.onUndoDelete(event.expenseId) }
                    .show()
            }
            is ExpenseUiEvent.ExpenseSaved -> {

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExpensesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExpensesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}