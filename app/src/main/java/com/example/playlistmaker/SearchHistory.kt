package com.example.playlistmaker

import android.content.SharedPreferences
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
        return gson.fromJson(json, Array<Track>::class.java).toList()
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
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
    }
}
