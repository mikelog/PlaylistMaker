package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Track

interface TrackRepository {
    fun search(query: String, callback: (List<Track>?, isNetworkError: Boolean) -> Unit)
}
