package com.example.playlistmaker

interface SearchHistoryRepository {
    fun getHistory(): MutableList<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}