package com.kingstudio.spendwise.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.repository.PeriodRollover
import com.kingstudio.spendwise.data.util.DateRangeCalculator
import com.kingstudio.spendwise.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var periodRollover: PeriodRollover
    private val launchPeriodKey = DateRangeCalculator.currentPeriodKey()

    private val fullScreenDestinations = setOf(
        R.id.incomeSetupFragment,
        R.id.createBudgetFragment,
//        R.id.profileSetupFragment

        R.id.addEditExpenseFragment,
        R.id.incomeUpdateFragment,
        R.id.budgetUpdateFragment,
//        R.id.profileUpdateFragment

        R.id.searchFragment
    )

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


      val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
              as NavHostFragment

      val navController = navHostFragment.navController

      binding.bottomNavigationView.setupWithNavController(navController)

      navController.addOnDestinationChangedListener { _, destination, _ ->
          val isFullScreen = destination.id in fullScreenDestinations
          binding.bottomNavigationView.visibility = if(isFullScreen) View.GONE else View.VISIBLE
          binding.floatingActionButton.visibility = if(isFullScreen) View.GONE else View.VISIBLE
      }


      binding.floatingActionButton.setOnClickListener {
          if(navController.currentDestination?.id != R.id.fabActionSheet) {
              navController.navigate(R.id.fabActionSheet)
          }
      }

    }

    override fun onStart() {
        super.onStart()
        if(DateRangeCalculator.currentPeriodKey() != launchPeriodKey) {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
            return
        }
        lifecycleScope.launch { periodRollover.ensureCurrentPeriod() }
    }
}