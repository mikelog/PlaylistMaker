package com.example.playlistmaker

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.view.WindowCompat

fun Activity.applyEdgeToEdge(
    rootView: View,
    topView: View? = null
) {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

        topView?.let { toolbar ->
            val params = toolbar.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = statusBars.top
            toolbar.layoutParams = params
        }

        view.setPadding(
            view.paddingLeft,
            view.paddingTop,
            view.paddingRight,
            navigationBars.bottom
        )

        insets
    }
}
