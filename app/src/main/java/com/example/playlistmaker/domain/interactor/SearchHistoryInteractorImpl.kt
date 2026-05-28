package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {
    override suspend fun getHistory(): List<Track> = repository.getHistory()
    override suspend fun addTrack(track: Track) = repository.addTrack(track)
    override fun clearHistory() = repository.clearHistory()
}
