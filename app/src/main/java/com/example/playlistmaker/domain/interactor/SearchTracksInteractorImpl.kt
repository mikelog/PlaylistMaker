package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchTracksInteractorImpl(
    private val repository: TrackRepository
) : SearchTracksInteractor {
    override fun search(query: String, consumer: (List<Track>?, isNetworkError: Boolean) -> Unit) {
        repository.search(query, consumer)
    }
}
