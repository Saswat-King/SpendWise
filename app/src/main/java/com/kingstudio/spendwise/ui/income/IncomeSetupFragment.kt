package com.kingstudio.spendwise.ui.income

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.local.entity.IncomeFrequency
import com.kingstudio.spendwise.databinding.FragmentIncomeSetupBinding
import com.kingstudio.spendwise.ui.common.SupportedCurrencies
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IncomeSetupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class IncomeSetupFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentIncomeSetupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IncomeSetupViewModel by viewModels()
    private var isBindingFields = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIncomeSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }


    // Listeners

    private fun setupListeners() {
        binding.incomeFrequencyTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val frequency = if(tab.position == 0) IncomeFrequency.MONTHLY else IncomeFrequency.YEARLY
                viewModel.onFrequencySelected(frequency)
            }
            override fun onTabReselected(p0: TabLayout.Tab?) {}
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
        })

        binding.etPrimaryIncomeAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(isBindingFields) return
                viewModel.onPrimaryIncomeChanged(s?.toString() ?: "")
            }
        })

        binding.currencySelectorContainer.setOnClickListener { showCurrencyPicker() }

        binding.bonusIncomeCard.setOnClickListener {
            showAmountInputDialog(
                title = "Bonus Amount",
                currentAmount = viewModel.formState.value.bonusAmount
            ) { amount -> viewModel.onBonusChanged(amount) }
        }

        binding.freelanceIncomeCard.setOnClickListener {
            showAmountInputDialog(
                title = "Freelance Income Amount",
                currentAmount = viewModel.formState.value.freelanceAmount
            ) { amount -> viewModel.onFreelanceChanged(amount) }
        }

        binding.otherIncomeCard.setOnClickListener {
            showAmountInputDialog(
                title = "Other Income Amount",
                currentAmount = viewModel.formState.value.otherAmount
            ) { amount -> viewModel.onOtherChanged(amount) }
        }

        binding.btnContinueIncome.setOnClickListener {
            viewModel.saveIncome()
        }
    }


    // Dialogs

    private fun showCurrencyPicker() {
        val labels = SupportedCurrencies.list
            .map {"${it.symbol} ${it.displayName} ($it.code)"}
            .toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Currency")
            .setItems(labels) { _, index ->
                binding.tvCurrencySymbol.text = SupportedCurrencies.list[index].symbol
                //TODO Datastore and Currency-dependent text fields

                renderFormState(viewModel.formState.value)
                renderSummary(viewModel.summary.value)
            }
            .show()
    }


    private fun showAmountInputDialog(
        title: String,
        currentAmount: String, onSave: (String) -> Unit ) {

        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(currentAmount)
            hint = "0"
            setSelection(text.length)
        }

        val container = FrameLayout(requireContext()).apply {
            val paddingH = (24 * resources.displayMetrics.density).toInt()
            val paddingTop = (12 * resources.displayMetrics.density).toInt()
            setPadding(paddingH,paddingTop,paddingH,0)
            addView(editText)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(container)
            .setPositiveButton("Save") {_,_ -> onSave(editText.text.toString())}
            .setNegativeButton("Cancel", null)
            .show()

        editText.requestFocus()
        editText.post {
            dialog.window?.let { window ->
                WindowCompat.getInsetsController(window, editText).show(WindowInsetsCompat.Type.ime())
            }
        }
    }

    // Observing ViewModel

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.formState.collect { renderFormState(it) } }
                launch { viewModel.summary.collect { renderSummary(it) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }

    private fun renderFormState(state: IncomeFormState) {
        binding.tvIncomeSetupTitle.text = state.screenTitle
        binding.tvIncomeSetupSubtitle.text = state.subTitle
        binding.btnContinueIncome.text = state.buttonLabel
        binding.btnContinueIncome.isEnabled = !state.isSaving

        if(binding.etPrimaryIncomeAmount.text.toString() != state.primaryIncomeAmount) {
            isBindingFields = true
            binding.etPrimaryIncomeAmount.setText(state.primaryIncomeAmount)
            binding.etPrimaryIncomeAmount.setSelection(binding.etPrimaryIncomeAmount.text?.length ?: 0)
            isBindingFields = false
        }

        binding.etPrimaryIncomeAmount.error = state.primaryIncomeError

        val frequencyTabIndex = if(state.frequency == IncomeFrequency.MONTHLY) 0 else 1
        if(binding.incomeFrequencyTabLayout.selectedTabPosition != frequencyTabIndex ) {
            binding.incomeFrequencyTabLayout.getTabAt(frequencyTabIndex)?.select()
        }

        binding.tvBonusIncomeAmount.text = formatAmount(state.bonusAmount)
        binding.tvFreelanceIncomeAmount.text = formatAmount(state.freelanceAmount)
        binding.tvOtherIncomeAmount.text = formatAmount(state.otherAmount)
    }

    private fun renderSummary(summary: IncomeSummary) {
        val symbol = binding.tvCurrencySymbol.text.toString()
        binding.tvIncomeSummaryAmount.text = getString(R.string.income_summary_amount, symbol, summary.totalIncome)
        binding.tvIncomeSummarySubtitle.text =  getString(R.string.total_period_income, summary.periodLabel)
        binding.tvBaseIncomeAmount.text = getString(R.string.base_income_amount, symbol, summary.baseIncome)
        binding.tvAdditionalIncomeAmount.text = getString(R.string.additional_income_amount, symbol, summary.additionalIncome)
        binding.tvTotalIncomeAmount.text =  getString(R.string.total_income_amount, symbol, summary.totalIncome)
    }

    private fun handleEvent(event: IncomeUiEvent) {
        when(event) {
            is IncomeUiEvent.IncomeSaved -> {
                // TODO navigate
            }
            is IncomeUiEvent.LowIncomeWarning -> {
                Snackbar.make(binding.root,
                    "The entered income seems unusually low, please verify.",
                    Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun formatAmount(raw: String): String {
        val symbol = binding.tvCurrencySymbol.text.toString()
        val value = raw.toDoubleOrNull() ?: 0.0
        return "$symbol${"%,.0f".format(value)}"
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
         * @return A new instance of fragment IncomeSetupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IncomeSetupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}