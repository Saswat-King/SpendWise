package com.kingstudio.spendwise.ui.dashboard

import android.graphics.Color
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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.local.dao.ExpenseDao
import com.kingstudio.spendwise.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private val budgetAdapter = DashboardBudgetCategoriesAdapter()
    private val recentExpenseAdapter = DashboardRecentExpensesAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerViews()
        observeUiState()
    }

    private fun setUpRecyclerViews() {
        binding.rvBudgetCategoriesPreview.apply {
            layoutManager  = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
            setHasFixedSize(true)
        }
        binding.rvRecentExpensesPreview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentExpenseAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when(state) {
                        is DashboardScreenState.Loading -> {
                            // Loading
                        }
                        is DashboardScreenState.Success -> renderState(state.data)
                    }
                }
            }
        }
    }

    private fun renderState(data: DashboardUiState) {
        binding.tvGreeting.text = data.greeting

        // Balance Card
        binding.tvAvailableBalance.text = getString(R.string.available_balance, data.availableBalance)
        binding.tvIncomeAmount.text = getString(R.string.income_amount, data.income)
        binding.tvExpenseAmount.text = getString(R.string.expense_amount, data.expenses)
        renderBalanceChart(data.spendingTrendPoints)

        // Budget Overview
        if(data.budgetOverview != null) {
            binding.budgetProgressContainer.visibility = View.VISIBLE
            binding.rvBudgetCategoriesPreview.visibility = View.VISIBLE
            binding.budgetCircularProgress.progress = data.budgetOverview.utilizationPercent
            binding.tvBudgetPercent.text = getString(R.string.dashboard_budget_percent, data.budgetOverview.utilizationPercent)
            binding.tvBudgetUsedAmount.text =  getString(R.string.dashboard_budget_used_amount, data.budgetOverview.totalSpent)
            binding.tvBudgetTotalAmount.text = getString(R.string.dashboard_budget_total_amount,data.budgetOverview.totalBudget)
            budgetAdapter.submitList(data.budgetOverview.categoryProgress)
        }
        else {
            binding.budgetProgressContainer.visibility = View.GONE
            binding.rvBudgetCategoriesPreview.visibility = View.GONE
        }

        // Recent Expenses
        recentExpenseAdapter.submitList(data.recentExpenses)
        setupClickListeners()
    }

    private fun renderBalanceChart(points: List<ExpenseDao.DailyTotal>) {
        if(points.isEmpty()) {
            binding.balanceChart.clear()
            return
        }
        val entries = points.mapIndexed { index, daily -> Entry(index.toFloat(), daily.total.toFloat()) }
        val dataset = LineDataSet(entries,"").apply {
            color = Color.WHITE
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.WHITE
            fillAlpha = 40
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        binding.balanceChart.apply {
            data = LineData(dataset)
            description.isEnabled = false
            legend.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            setTouchEnabled(false)
            setDrawGridBackground(false)
            setDrawBorders(false)
            invalidate()
        }
    }

    private fun setupClickListeners() {
        //TODO other click listeners
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
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                DashboardFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}