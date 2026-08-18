package com.example.playlistmaker.ui.theme

import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Resolves a day/night theme attribute (defined in themes.xml) to a Compose [Color],
 * so Compose screens stay visually consistent with the still-XML-themed parts of the app.
 */
@Composable
fun colorAttr(@AttrRes id: Int): Color {
    val context = LocalContext.current
    val typedValue = TypedValue()
    context.theme.resolveAttribute(id, typedValue, true)
    return Color(typedValue.data)
}
