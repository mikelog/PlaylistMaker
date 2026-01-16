package com.example.playlistmaker

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}