package com.example.playlistmaker.domain.interactor

interface ThemeInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}
