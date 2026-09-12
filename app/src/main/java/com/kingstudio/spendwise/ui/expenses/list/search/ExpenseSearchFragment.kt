package com.kingstudio.spendwise.ui.expenses.list.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.databinding.FragmentExpenseSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.checkerframework.checker.index.qual.GTENegativeOne

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExpenseSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class ExpenseSearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentExpenseSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpenseSearchViewModel by viewModels()
    private lateinit var resultAdapter: ExpenseSearchAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseSearchBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        focusSearchField()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun focusSearchField() {
        binding.etSearchExpenses.requestFocus()
        binding.etSearchExpenses.post {
            WindowCompat.getInsetsController(requireActivity().window,
                binding.etSearchExpenses).show(WindowInsetsCompat.Type.ime())
        }
    }

    private fun setupRecyclerView() {
        resultAdapter = ExpenseSearchAdapter { expenseWithCategory ->
            // TODO navigate to update screen nav graph
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter
        }

    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.etSearchExpenses.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s?.toString() ?: "")
            }
        })

        binding.etSearchExpenses.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                WindowCompat.getInsetsController(requireActivity().window,
                    binding.etSearchExpenses).hide(WindowInsetsCompat.Type.ime())
                true
            }
            else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when(state) {
                        is ExpenseSearchUiState.Idle -> {
                            binding.rvSearchResults.visibility = GONE
                            binding.emptyState.visibility = GONE
                            resultAdapter.submitList(emptyList())
                        }

                        is  ExpenseSearchUiState.NoResults -> {
                            binding.rvSearchResults.visibility = GONE
                            binding.emptyState.visibility = VISIBLE
                            resultAdapter.submitList(emptyList())
                        }

                        is ExpenseSearchUiState.Results -> {
                            binding.rvSearchResults.visibility = VISIBLE
                            binding.emptyState.visibility  = GONE
                            resultAdapter.submitList(state.expenses)
                        }
                    }
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
         * @return A new instance of fragment ExpenseSearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExpenseSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}