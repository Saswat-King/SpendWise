package com.kingstudio.spendwise.ui.expenses.addedit

import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.local.entity.CategoryEntity
import com.kingstudio.spendwise.data.util.CurrencyFormatter
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import com.kingstudio.spendwise.data.util.RelativeDateFormatter
import com.kingstudio.spendwise.databinding.FragmentAddExpenseBinding
import com.kingstudio.spendwise.ui.common.CategoryIconResolver
import com.kingstudio.spendwise.ui.common.DecimalInputFilter
import com.kingstudio.spendwise.ui.expenses.list.ExpenseUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import com.google.android.material.R.id.snackbar_text
import java.time.LocalDate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val DATE_PICKER_TAG = "expense_date_picker"

/**
 * A simple [Fragment] subclass.
 * Use the [AddExpenseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditExpenseViewModel by viewModels()
    private var limitToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInputFilters()
        setupListeners()
        observeViewmodel()
        reattachDatePickerListener()
        setupInsets()

//        if(savedInstanceState == null && viewModel.formState.value.mode == ExpenseFormMode.ADD) {
//            focusAmountField()
//        }
    }


    // Setup

    private fun setupInputFilters() {
        binding.etExpenseAmount.filters = arrayOf(DecimalInputFilter())

        val titleLimitObserver = InputFilter { _, start, end, dest, dstart, dend ->
            val newLength = dest.length - (dend - dstart) + (end - start)
            if(newLength > AddEditExpenseViewModel.TITLE_MAX_LENGTH) showTitleLimitToast()
            null
        }
        binding.etExpenseTitle.filters = arrayOf(titleLimitObserver, *binding.etExpenseTitle.filters)
    }


    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.addExpenseRoot) { _, insets ->
            val bottom = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom
            binding.addExpenseScrollView.updatePadding(bottom = bottom)

            if(insets.isVisible(WindowInsetsCompat.Type.ime()) && binding.etExpenseNote.hasFocus()) {
                binding.addExpenseScrollView.post {
                    binding.addExpenseScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
            insets
        }
    }

    private fun setupListeners() {
        binding.btnAddExpenseBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etExpenseTitle.doAfterTextChanged { viewModel.onTitleChanged(it?.toString().orEmpty()) }
        binding.etExpenseAmount.doAfterTextChanged { viewModel.onAmountChanged(it?.toString().orEmpty()) }
        binding.etExpenseNote.doAfterTextChanged { viewModel.onNoteChanged(it?.toString().orEmpty()) }

        binding.categorySelectorCard.setOnClickListener { showCategoryPicker() }
        binding.dateSelectorCard.setOnClickListener { showDatePicker() }
        binding.btnSaveExpense.setOnClickListener { viewModel.saveExpense() }
    }

    private fun observeViewmodel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.formState, viewModel.categories) { state, categories ->
                        state to categories }.collect {
                        (state, categories) -> renderFormState(state, categories)
                    }
                }

                launch {
                    viewModel.currencyCode.collect {
                        binding.tvExpenseCurrencySymbol.text = CurrencyFormatter.symbolFor(it)
                    }
                }

                launch {
                    viewModel.messages.collect {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }

                launch {
                    viewModel.events.collect { handleEvent(it) }
                }
            }
        }
    }


    // Rendering

    private fun renderFormState(state: ExpenseFormState, categories: List<CategoryEntity>) {
        binding.tvAddExpenseTitle.text = state.screenTitle
        binding.tvAddExpenseSubtitle.isVisible = state.mode == ExpenseFormMode.ADD
        binding.btnSaveExpense.text = state.buttonLabel
        binding.btnSaveExpense.isEnabled = !state.isSaving

        syncText(binding.etExpenseTitle, state.title)
        syncText(binding.etExpenseAmount, state.amount)
        syncText(binding.etExpenseNote, state.note)

        binding.etExpenseTitle.setErrorIfChanged(state.titleError)
        binding.etExpenseAmount.setErrorIfChanged(state.amountError)

        binding.tvNoteCounter.text = getString(
            R.string.note_counter,
            state.note.length, AddEditExpenseViewModel.NOTE_MAX_LENGTH)

        binding.tvSelectedDate.text = RelativeDateFormatter.formatFullDate(state.date)
        renderCategory(categories, state.categoryId, hasError = state.categoryError != null)
    }

    private fun renderCategory(categories: List<CategoryEntity>, categoryId: Long?, hasError: Boolean) {
        val category = categories.find { it.id == categoryId }

        if(category == null) {
            binding.tvSelectedCategory.text = getString(R.string.select_category)
            binding.imgCategoryIcon.setImageResource(R.drawable.ic_category)
            binding.imgCategoryIcon.clearColorFilter()

            binding.categoryIconContainer.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.sw_surface_primary)
            )
        }
        else {
            binding.tvSelectedCategory.text = category.name
            binding.imgCategoryIcon.setImageResource(CategoryIconResolver.getIconResource(category.iconKey))
            binding.imgCategoryIcon.setColorFilter(CategoryIconResolver.getIconColor(category.colorHex))

            binding.categoryIconContainer.setCardBackgroundColor(
                CategoryIconResolver.getLightBackgroundColor(category.colorHex)
            )
        }

        binding.categorySelectorCard.strokeColor = if (hasError) {
            MaterialColors.getColor(binding.categorySelectorCard, androidx.appcompat.R.attr.colorError)
        }
        else {
            ContextCompat.getColor(requireContext(), R.color.sw_outline_soft)
        }
    }


    private fun syncText(editText: EditText, value: String) {
        if(editText.text.toString() != value) {
            editText.setText(value)
            editText.setSelection(editText.text.length)
        }
    }


    private fun EditText.setErrorIfChanged(message: String?) {
        if(error?.toString() != message) error = message
    }


    private fun handleEvent(event: ExpenseUiEvent) {
        when(event) {
            is ExpenseUiEvent.ExpenseSaved -> {
                hideKeyboard()
                findNavController().popBackStack()
            }
            else -> Unit
        }
    }


    // Pickers

    private fun showCategoryPicker() {
        val categories = viewModel.categories.value
        if(categories.isEmpty()) return

        prepareForDialog()
        val selectedIndex = categories.indexOfFirst { it.id == viewModel.formState.value.categoryId }
        val budgetedIds = viewModel.budgetedCategoryIds.value

        val labels = categories.map {
            if (it.id in budgetedIds) it.name else getString(R.string.category_not_in_budget_label, it.name)
        }.toTypedArray()


        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_category)
            .setSingleChoiceItems(labels, selectedIndex) { dialog, index ->
                val picked = categories[index]
                dialog.dismiss()

                if(!viewModel.onCategorySelected(picked.id)) {
                    Snackbar.make(
                        binding.root, getString(R.string.category_not_in_budget, picked.name),
                        Snackbar.LENGTH_LONG
                    ).apply {
                        view.findViewById<TextView>(snackbar_text).maxLines = 4
                    }.show()
                }
            }
            .show()

    }


    private fun showDatePicker() {
        prepareForDialog()

        val today = RelativeDateFormatter.toLocalDate(System.currentTimeMillis())
        val monthStart = utcMidnight(today.withDayOfMonth(1))
        val todayUtc = utcMidnight(today)

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.select_date)
            .setSelection(toPickerSelection(viewModel.formState.value.date).coerceIn( monthStart, todayUtc ))
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(monthStart)
                    .setEnd(todayUtc)
                    .setValidator(DateValidatorPointBackward.before(todayUtc))
                    .build()
            )
            .build()


        picker.addOnPositiveButtonClickListener { onDatePicked(it) }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }


    private fun utcMidnight(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()


    @Suppress("UNCHECKED_CAST")
    private fun reattachDatePickerListener() {
        (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<Long>)
            ?.addOnPositiveButtonClickListener { onDatePicked(it) }
    }


    private fun onDatePicked(utcMillis: Long) {
        viewModel.onDateSelected(fromPickerSelection(utcMillis,
            viewModel.formState.value.date))
    }


    private fun toPickerSelection(epochMillis: Long): Long =
        RelativeDateFormatter.toLocalDate(epochMillis).
            atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()


    private fun fromPickerSelection(utcMillis: Long, currentEpochMillis: Long): Long {
        val zone = ZoneId.systemDefault()
        val pickedDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        val timeOfDay = Instant.ofEpochMilli(currentEpochMillis).atZone(zone).toLocalTime()
        val merged = pickedDate.atTime(timeOfDay).atZone(zone).toInstant().toEpochMilli()
        return minOf(merged, System.currentTimeMillis())
    }


    // helpers

    private fun hideKeyboard() {
        WindowCompat.getInsetsController(requireActivity().window, binding.root)
            .hide(WindowInsetsCompat.Type.ime())
    }


    private fun prepareForDialog() {
        binding.root.clearFocus()
        hideKeyboard()
    }

    private fun showTitleLimitToast() {
        limitToast?.cancel()
        limitToast = Toast.makeText(
            requireContext(), getString(R.string.expense_title_limit_toast,
                AddEditExpenseViewModel.TITLE_MAX_LENGTH), Toast.LENGTH_SHORT
        ).also { it.show() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        limitToast?.cancel()
        _binding = null
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddExpenseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddExpenseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}