package com.kingstudio.spendwise.ui.common

import android.text.InputFilter
import android.text.Spanned

class DecimalInputFilter(
    private val maxDecimalDigits: Int = 2,
    private val maxValue: Double = MAX_AMOUNT
) : InputFilter {

    companion object {
        const val MAX_AMOUNT = 99_999_999.99
    }

    override fun filter(
        source: CharSequence,
        start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        val proposed = StringBuilder(dest)
            .replace(dstart,dend,source.subSequence(start,end).toString())
            .toString()

        if(proposed.isEmpty()) return null
        if(proposed.startsWith(".")) return ""

        if(proposed.count{ it == '.' } > 1) return ""

        val dotIndex = proposed.indexOf('.')
        if(dotIndex != -1 && proposed.length - dotIndex - 1 > maxDecimalDigits) return ""

        val numericValue = proposed.toDoubleOrNull() ?: return ""
        if(numericValue > maxValue) return ""

        return null
    }
}