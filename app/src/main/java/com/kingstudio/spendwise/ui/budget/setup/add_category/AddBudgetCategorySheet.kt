package com.kingstudio.spendwise.ui.budget.setup.add_category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kingstudio.spendwise.databinding.FragmentAddBudgetCategorySheetBinding
import com.kingstudio.spendwise.ui.budget.setup.BudgetSetupViewModel
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddBudgetCategorySheet.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddBudgetCategorySheet : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAddBudgetCategorySheetBinding? = null
    private val binding get() = _binding!!

    private val sheetViewModel: AddBudgetCategorySheetViewModel by viewModels()

    private val budgetSetUpViewModel: BudgetSetupViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var categoryAdapter: SelectCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddBudgetCategorySheetBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val excludedIds = budgetSetUpViewModel.formState.value.categoryInputs
            .map { it.category.id }
            .toSet()
        sheetViewModel.setExcludedCategoryIds(excludedIds)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        categoryAdapter = SelectCategoryAdapter { category ->
            sheetViewModel.onCategorySelected(category.id)
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = categoryAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val selectedId = sheetViewModel.selectedCategoryId.value
            val selectableCategory = sheetViewModel.displayItems.value
                .find { it.category.id == selectedId }?.category

            val amountText = binding.etAmount.text?.toString().orEmpty()
            val amountValue = amountText.toDoubleOrNull()

            if(selectableCategory == null) {
                return@setOnClickListener
            }

            if(amountValue == null || amountValue<=0.0) {
                binding.etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }

            budgetSetUpViewModel.onCategoryAdded(selectableCategory, amountText)
            dismiss()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sheetViewModel.displayItems.collect { items ->
                    categoryAdapter.submitList(items)
                }
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
         * @return A new instance of fragment AddBudgetCategorySheet.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddBudgetCategorySheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}