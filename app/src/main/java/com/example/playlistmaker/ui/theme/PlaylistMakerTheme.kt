package com.example.playlistmaker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.android.material.R as MaterialR

/**
 * Material3 components (Button, TabRow, Switch, …) fall back to Material3's baseline purple
 * palette unless a ColorScheme is supplied. This app's actual colors live in XML theme attrs
 * (colorOnPrimary = screen background, colorOnSecondary = text/accent), not the standard
 * Material attrs, so they must be mapped in explicitly to keep Compose screens visually
 * consistent with the still-XML-themed parts of the app.
 */
@Composable
fun PlaylistMakerTheme(content: @Composable () -> Unit) {
    val background = colorAttr(MaterialR.attr.colorOnPrimary)
    val onBackground = colorAttr(MaterialR.attr.colorOnSecondary)

    val colorScheme = MaterialTheme.colorScheme.copy(
        primary = onBackground,
        onPrimary = background,
        secondary = onBackground,
        onSecondary = background,
        background = background,
        onBackground = onBackground,
        surface = background,
        onSurface = onBackground
    )

    MaterialTheme(colorScheme = colorScheme, content = content)
}
