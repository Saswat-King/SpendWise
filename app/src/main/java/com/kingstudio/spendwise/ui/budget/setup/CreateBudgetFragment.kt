package com.kingstudio.spendwise.ui.budget.setup

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
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.databinding.FragmentCreateBudgetBinding
import com.kingstudio.spendwise.ui.budget.setup.add_category.AddBudgetCategorySheet
import com.kingstudio.spendwise.ui.common.FormMode
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateBudgetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateBudgetFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentCreateBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetSetupViewModel by viewModels()
    private lateinit var categoryAdapter: CreateBudgetCategoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateBudgetBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CreateBudgetCategoryAdapter { categoryId, amount ->
            viewModel.onAmountChanged(categoryId, amount)
        }

        binding.rvCreateBudgetCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupListeners() {
        binding.btnCreateBudgetBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAddBudgetCategory.setOnClickListener {
            viewModel.onAddCategoryClicked()
        }

        binding.btnContinueBudget.setOnClickListener {
            viewModel.saveBudget()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.formState.collect { renderFormState(it) } }
                launch { viewModel.summary.collect { renderSummary(it) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }


    private fun renderFormState(state: BudgetSetupFormState) {
        binding.tvCreateBudgetTitle.text = state.screenTitle
        binding.btnContinueBudget.text = state.buttonLabel
        binding.btnContinueBudget.isEnabled = !state.isSaving

        binding.btnCreateBudgetBack.setImageResource(
            if(state.mode == FormMode.UPDATE) R.drawable.ic_close else R.drawable.ic_arrow_back
        )

        categoryAdapter.submitList(state.categoryInputs)
    }


    private fun renderSummary(summary: BudgetSetupSummary) {
        // TODO Datastore and Currency-dependent text fields
        binding.tvCreateBudgetTotalAmount.text =
            getString(R.string.budget_summary_total_budget_amount, summary.totalBudget)
    }

    private fun handleEvent(event: BudgetSetupUiEvent) {
        when(event) {
            is BudgetSetupUiEvent.BudgetSaved -> {
                // TODO navigate to other
            }

            is BudgetSetupUiEvent.NavigateToAddCategory -> {
                AddBudgetCategorySheet().show(childFragmentManager,"AddBudgetCategorySheet")
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
         * @return A new instance of fragment CreateBudgetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateBudgetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}