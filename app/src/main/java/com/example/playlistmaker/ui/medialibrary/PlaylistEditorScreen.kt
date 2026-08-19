package com.example.playlistmaker.ui.medialibrary

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.ui.common.AppAlertDialog
import com.example.playlistmaker.ui.common.GlideAsyncImage
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme
import com.example.playlistmaker.ui.theme.colorAttr
import java.io.File

@Composable
fun PlaylistEditorScreen(
    toolbarTitle: String,
    submitButtonText: String,
    coverUri: Uri?,
    fallbackCoverPath: String,
    initialName: String?,
    initialDescription: String?,
    confirmDiscardOnBack: Boolean,
    onCoverAreaClick: () -> Unit,
    onSubmit: (name: String, description: String) -> Unit,
    onBack: () -> Unit
) {
    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialName) {
        if (name.isBlank() && !initialName.isNullOrBlank()) name = initialName
    }
    LaunchedEffect(initialDescription) {
        if (description.isBlank() && !initialDescription.isNullOrBlank()) description = initialDescription
    }

    fun handleBack() {
        val hasUnsavedData = name.isNotBlank() || description.isNotBlank() || coverUri != null
        if (confirmDiscardOnBack && hasUnsavedData) {
            showExitDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(onBack = ::handleBack)

    PlaylistMakerTheme {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = ::handleBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24),
                    contentDescription = null,
                    tint = textColor
                )
            }
            Text(
                text = toolbarTitle,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 19.sp
            )
        }

        val coverModel: Any? = coverUri
            ?: fallbackCoverPath.takeIf { it.isNotEmpty() }?.let { File(it) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onCoverAreaClick),
            contentAlignment = Alignment.Center
        ) {
            if (coverModel != null) {
                GlideAsyncImage(
                    model = coverModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorAttr(R.attr.yp_gray_color).copy(alpha = 0.08f))
                        .border(1.dp, colorAttr(R.attr.yp_gray_color), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_photo),
                        contentDescription = null,
                        tint = colorAttr(R.attr.yp_gray_color)
                    )
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.playlist_name_hint)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = colorAttr(R.attr.yp_gray_color),
                unfocusedBorderColor = colorAttr(R.attr.yp_gray_color),
                focusedLabelColor = colorAttr(R.attr.yp_gray_color),
                unfocusedLabelColor = colorAttr(R.attr.yp_gray_color)
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.playlist_description_hint)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = colorAttr(R.attr.yp_gray_color),
                unfocusedBorderColor = colorAttr(R.attr.yp_gray_color),
                focusedLabelColor = colorAttr(R.attr.yp_gray_color),
                unfocusedLabelColor = colorAttr(R.attr.yp_gray_color)
            )
        )

        Box(modifier = Modifier.weight(1f))

        Button(
            onClick = { onSubmit(name.trim(), description.trim()) },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.blue_background),
                contentColor = Color.White,
                disabledContainerColor = colorResource(R.color.yp_gray),
                disabledContentColor = Color.White
            )
        ) {
            Text(submitButtonText)
        }
    }
    }

    if (showExitDialog) {
        AppAlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_playlist_dialog_title)) },
            text = { Text(stringResource(R.string.exit_playlist_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.blue_background)
                    )
                ) { Text(stringResource(R.string.finish)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.blue_background)
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
