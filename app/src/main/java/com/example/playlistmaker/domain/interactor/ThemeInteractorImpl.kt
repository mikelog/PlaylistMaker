package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.ThemeRepository

class ThemeInteractorImpl(
    private val repository: ThemeRepository
) : ThemeInteractor {
    override fun isDarkTheme(): Boolean = repository.isDarkTheme()
    override fun setDarkTheme(enabled: Boolean) = repository.setDarkTheme(enabled)
}
