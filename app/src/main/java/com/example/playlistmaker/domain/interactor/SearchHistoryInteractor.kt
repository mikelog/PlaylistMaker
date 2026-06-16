package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    suspend fun getHistory(): List<Track>
    suspend fun addTrack(track: Track)
    fun clearHistory()
}
