package com.example.playlistmaker.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Matches the app's ThemeOverlay.PlaylistMaker.Dialog: dialogs always render on a plain white
 * surface with dark text and a small corner radius, regardless of the app's day/night theme —
 * unlike the rest of the UI, this one doesn't follow colorOnPrimary/colorOnSecondary.
 */
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        containerColor = Color.White,
        titleContentColor = Color(0xFF1A1B22),
        textContentColor = Color(0xFF1A1B22),
        shape = RoundedCornerShape(4.dp)
    )
}
