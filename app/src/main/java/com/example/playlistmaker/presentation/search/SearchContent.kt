package com.example.playlistmaker.presentation.search

import com.example.playlistmaker.domain.models.Track

sealed class SearchContent {
    object Idle : SearchContent()
    object Loading : SearchContent()
    data class Tracks(val tracks: List<Track>) : SearchContent()
    object Empty : SearchContent()
    object NetworkError : SearchContent()
}
