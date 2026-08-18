package com.example.playlistmaker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme
import com.example.playlistmaker.ui.theme.colorAttr

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onShareApp: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenTerms: () -> Unit
) {
    val state by viewModel.screenState.observeAsState()
    val screenState = state ?: return

    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)

    PlaylistMakerTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(start = 16.dp, top = 16.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dark_theme),
                    color = textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = screenState.isDarkTheme,
                    onCheckedChange = { viewModel.onThemeToggled(it) }
                )
            }

            SettingsRow(
                text = stringResource(R.string.share_app),
                iconRes = R.drawable.ic_share_24,
                textColor = textColor,
                onClick = onShareApp
            )
            SettingsRow(
                text = stringResource(R.string.support),
                iconRes = R.drawable.ic_support_24,
                textColor = textColor,
                onClick = onOpenSupport
            )
            SettingsRow(
                text = stringResource(R.string.user_agreement),
                iconRes = R.drawable.ic_arrow_right_24,
                textColor = textColor,
                onClick = onOpenTerms
            )
        }
    }
}

@Composable
private fun SettingsRow(
    text: String,
    iconRes: Int,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
    }
}
