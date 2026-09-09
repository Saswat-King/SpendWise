package com.kingstudio.spendwise.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.kingstudio.spendwise.ui.dashboard.DashboardFragment
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.databinding.ActivityMainBinding
import com.kingstudio.spendwise.ui.budget.setup.CreateBudgetFragment
import com.kingstudio.spendwise.ui.expenses.list.ExpensesFragment
import com.kingstudio.spendwise.ui.income.IncomeSetupFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNavigationView.updatePadding(bottom = systemBars.bottom)
            insets
        }


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateBudgetFragment())
                .commit()
        }

    }
}