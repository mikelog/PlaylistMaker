package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track

interface SearchTracksInteractor {
    fun search(query: String, consumer: (List<Track>?, isNetworkError: Boolean) -> Unit)
}