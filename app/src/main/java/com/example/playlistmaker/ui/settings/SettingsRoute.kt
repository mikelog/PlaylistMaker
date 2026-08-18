package com.example.playlistmaker.ui.settings

import androidx.compose.runtime.Composable
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute() {
    val viewModel: SettingsViewModel = koinViewModel()

    SettingsScreen(
        viewModel = viewModel,
        onShareApp = { viewModel.onShareAppClicked() },
        onOpenSupport = { viewModel.onOpenSupportClicked() },
        onOpenTerms = { viewModel.onOpenTermsClicked() }
    )
}
