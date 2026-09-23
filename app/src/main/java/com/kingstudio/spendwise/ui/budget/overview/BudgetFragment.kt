package com.kingstudio.spendwise.ui.budget.overview

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.util.BudgetPerformanceCalculator
import com.kingstudio.spendwise.data.util.RelativeDateFormatter
import com.kingstudio.spendwise.databinding.FragmentBudgetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BudgetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class BudgetFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetScreenViewModel by viewModels()

    private lateinit var analyticsAdapter: BudgetAnalyticsAdapter
    private lateinit var categoryStatusAdapter: BudgetCategoryStatusAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
       _binding = FragmentBudgetBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupListeners()
        observeViewModel()
    }


    private fun setupRecyclerViews() {
        analyticsAdapter = BudgetAnalyticsAdapter()
        binding.rvBudgetAnalyticsCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = analyticsAdapter
        }

        categoryStatusAdapter = BudgetCategoryStatusAdapter()
        binding.rvBudgetCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryStatusAdapter
        }
    }


    private fun setupListeners() {
        binding.btnBudgetMonth.setOnClickListener { showPeriodPicker() }
    }


    private fun showPeriodPicker() {
        val periods = viewModel.availablePeriods.value
        if(periods.isEmpty()) return

        val labels = periods.map { RelativeDateFormatter.formatPeriodKey(it) }.toTypedArray()
        val currentIndex = periods.indexOf(viewModel.selectedPeriodKey.value).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Month")
            .setSingleChoiceItems(labels, currentIndex) { dialog, index ->
                viewModel.onPeriodSelected(periods[index])
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> renderState(state) }
            }
        }
    }

    private fun renderState(state: BudgetScreenState) {
        when(state) {
            is BudgetScreenState.Loading -> {}
            is BudgetScreenState.Empty -> renderEmptyState()
            is BudgetScreenState.Success -> renderSuccess(state.data)
        }
    }


    private fun renderEmptyState() {
        binding.tvTotalBudgetAmount.text = getString(R.string.expense_no_data)
        binding.tvTotalSpentAmount.text = getString(R.string.expense_no_data)
        binding.tvRemainingBudgetAmount.text = getString(R.string.expense_no_data)
        binding.budgetSummaryProgress.progress = 0
        binding.tvBudgetProgressInfo.text = getString(R.string.budget_progress_empty_message)

        binding.budgetDonutChart.clear()
        binding.budgetPerformanceChart.clear()
        analyticsAdapter.submitList(emptyList())
        categoryStatusAdapter.submitList(emptyList())
    }


    private fun renderSuccess(data: BudgetScreenUiState) {
        binding.btnBudgetMonth.text = RelativeDateFormatter.formatPeriodKey(data.selectedPeriodKey)

        val overview = data.overview
        binding.tvTotalBudgetAmount.text = getString(R.string.budget_total_amount, overview.totalBudget)
        binding.tvTotalSpentAmount.text = getString(R.string.budget_total_spent, overview.totalSpent)
        binding.tvRemainingBudgetAmount.text = getString(R.string.budget_remaining_amount, overview.remaining)


        val utilizationPercent = if( overview.totalBudget > 0) {
            ((overview.totalSpent / overview.totalBudget) * 100).toInt().coerceIn(0,100)
        } else 0

        binding.budgetSummaryProgress.progress = utilizationPercent
        binding.tvBudgetProgressInfo.text = getString(R.string.budget_progress_info, utilizationPercent)


        renderDonutChart(data.spendShares)
        renderPerformanceChart(data.performancePoints)

        analyticsAdapter.submitList(data.spendShares)
        categoryStatusAdapter.submitList(data.categoryRows)
    }


    private fun renderDonutChart(spendShares: List<CategorySpendShare>) {
        if(spendShares.isEmpty()) {
            binding.budgetDonutChart.clear()
            return
        }

        val entries = spendShares.map { PieEntry(it.spentAmount.toFloat(), it.categoryName) }
        val colors = spendShares.map {
            try { it.colorHex.toColorInt() } catch (e: IllegalArgumentException) {Color.GRAY}
        }


        val dataSet = PieDataSet(entries,"").apply {
            this.colors = colors
            sliceSpace = 2f
            setDrawValues(true)
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        binding.budgetDonutChart.apply {
            data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(binding.budgetDonutChart))
            }

            setUsePercentValues(true)
            setDrawEntryLabels(false)
            description.isEnabled = false
            legend.isEnabled = false
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 55f
            transparentCircleRadius = 58f
            setTouchEnabled(false)
            invalidate()
        }
    }


    private fun renderPerformanceChart(points: List<BudgetPerformanceCalculator.Point>) {
        if(points.isEmpty()) {
            binding.budgetPerformanceChart.clear()
            return
        }

        val budgetEntries = points.mapIndexed { index, point ->
            Entry(index.toFloat(),point.cumulativeBudget.toFloat())
        }

        val actualEntries = points.mapIndexedNotNull { index, point ->
            point.cumulativeActual?.let { Entry(index.toFloat(), it.toFloat()) }
        }


        val budgetSet = LineDataSet(budgetEntries, "Budget").apply {
            color = ContextCompat.getColor(requireContext(), R.color.sw_outline_soft)
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            enableDashedLine(6f,4f,0f)
        }

        val actualSet = LineDataSet(actualEntries, "Actual Spending").apply {
            color = ContextCompat.getColor(requireContext(), R.color.sw_primary)
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor =  ContextCompat.getColor(requireContext(), R.color.sw_primary)
            fillAlpha = 30
        }

        binding.budgetPerformanceChart.apply {
            data = LineData(budgetSet, actualSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            xAxis.isEnabled = true
            axisLeft.isEnabled = true
            axisLeft.setDrawGridLines(false)
            setTouchEnabled(false)
            invalidate()
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
         * @return A new instance of fragment BudgetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BudgetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}