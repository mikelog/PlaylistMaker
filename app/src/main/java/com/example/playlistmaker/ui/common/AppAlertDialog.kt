package com.example.playlistmaker.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.playlistmaker.ui.theme.AppColors

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
        containerColor = AppColors.DialogContainer,
        titleContentColor = AppColors.DialogContent,
        textContentColor = AppColors.DialogContent,
        shape = RoundedCornerShape(4.dp)
    )
}
