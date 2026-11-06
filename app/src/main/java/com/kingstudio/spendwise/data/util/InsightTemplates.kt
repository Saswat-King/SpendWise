package com.kingstudio.spendwise.data.util

import android.content.Context
import com.kingstudio.spendwise.R
import com.kingstudio.spendwise.data.model.InsightFact
import com.kingstudio.spendwise.data.model.InsightType

object InsightTemplates {
    fun text(fact: InsightFact, currencyCode: String, context: Context): String {
        val amount = CurrencyFormatter.formatCompact(fact.amount, currencyCode)
        return when(fact.type) {
           InsightType.PROJECTION ->
               if(fact.percent > 100) {
                   context.getString(R.string.insight_spending_warning_exceeds, amount,
                       fact.percent - 100
                   )
               }
               else {
                   context.getString(R.string.insight_spending_warning_within, amount, fact.percent)
               }

           InsightType.CATEGORY_SHIFT -> context.getString(R.string.insight_category_shift,
               fact.label, fact.amount, amount)


           InsightType.WEEKEND_PATTERN -> context.getString(R.string.insight_weekend_pattern,
              fact.percent, fact.label, amount)


           InsightType.UNUSUAL_EXPENSE -> context.getString(R.string.insight_unusual_expense,
              fact.label, amount, fact.percent)


           InsightType.NO_SPEND_DAYS -> context.getString(R.string.insight_no_spend_days, fact.amount.toInt())
           InsightType.SMALL_SPENDS -> context.getString(R.string.insight_small_spends, amount, fact.percent)

            InsightType.SAVING_TIP -> when (fact.label) {
                "SAVINGS" -> context.getString(R.string.insight_saving_tip_savings)
                "BUDGET" -> context.getString(R.string.insight_saving_tip_budget)
                else -> context.getString(R.string.insight_saving_tip_default)
            }
        }
    }
}