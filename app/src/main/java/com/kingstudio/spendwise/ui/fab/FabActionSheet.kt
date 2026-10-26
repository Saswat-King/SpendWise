package com.kingstudio.spendwise.ui.fab


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.kingstudio.spendwise.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kingstudio.spendwise.databinding.SheetFabActionsBinding

class FabActionSheet : BottomSheetDialogFragment() {
    private var _binding: SheetFabActionsBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.ThemeOverlay_SpendWise_BottomSheet

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SheetFabActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rowAddExpense.setOnClickListener { navigate(R.id.action_fabSheet_to_addExpense) }
        binding.rowUpdateBudget.setOnClickListener { navigate(R.id.action_fabSheet_to_budgetUpdate) }
        binding.rowUpdateIncome.setOnClickListener { navigate(R.id.action_fabSheet_to_incomeUpdate) }
        binding.btnCloseQuickActions.setOnClickListener { findNavController().popBackStack() }
    }

    private fun navigate(actionId: Int) {
        val navController = findNavController()
        if(navController.currentDestination?.id == R.id.fabActionSheet) {
            navController.navigate(actionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}