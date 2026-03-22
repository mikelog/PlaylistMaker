package com.example.playlistmaker.presentation.search

import com.example.playlistmaker.domain.models.Track

data class ScreenState(
    val query: String = "",
    val searchContent: SearchContent = SearchContent.Idle,
    val historyTracks: List<Track>? = null  // null = скрыта, список = видна
)
