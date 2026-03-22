package com.example.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.settings.model.ThemeSettings
import com.example.playlistmaker.domain.sharing.SharingInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor,
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    data class SettingsScreenState(
        val isDarkTheme: Boolean
    )

    private val _screenState = MutableLiveData(
        SettingsScreenState(
            isDarkTheme = settingsInteractor.getThemeSettings().isDarkTheme
        )
    )
    val screenState: LiveData<SettingsScreenState> = _screenState

    // ---- Тема ----
    fun onThemeToggled(enabled: Boolean) {
        settingsInteractor.updateThemeSetting(ThemeSettings(isDarkTheme = enabled))
        _screenState.value = SettingsScreenState(isDarkTheme = enabled)
    }

    // ---- Sharing ----
    fun onShareAppClicked() {
        sharingInteractor.shareApp()
    }

    fun onOpenTermsClicked() {
        sharingInteractor.openTerms()
    }

    fun onOpenSupportClicked() {
        sharingInteractor.openSupport()
    }
}
