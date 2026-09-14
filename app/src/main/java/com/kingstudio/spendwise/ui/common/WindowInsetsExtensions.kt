package com.kingstudio.spendwise.ui.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.applyBottomNavigationBarInset() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->

        val bottomInset =
            insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

        view.updatePadding(
            bottom = bottomInset
        )

        insets
    }
}