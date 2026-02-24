package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.data.dto.TrackHistoryDto
import com.example.playlistmaker.data.mapper.toHistoryDto
import com.example.playlistmaker.data.mapper.toTrack
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.google.gson.Gson

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SearchHistoryRepository {

    companion object {
        private const val HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null) ?: return emptyList()
        // Читаем как список DTO, затем маппим в доменные модели
        return gson.fromJson(json, Array<TrackHistoryDto>::class.java)
            .mapNotNull { it.toTrack() }
    }

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.subList(MAX_HISTORY_SIZE, history.size).clear()
        }
        save(history)
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(HISTORY_KEY).apply()
    }

    private fun save(history: List<Track>) {
        // Сохраняем как список DTO, не доменных моделей
        val dtoList = history.map { it.toHistoryDto() }
        sharedPreferences.edit().putString(HISTORY_KEY, gson.toJson(dtoList)).apply()
    }
}
