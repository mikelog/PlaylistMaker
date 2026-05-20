package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow

class SearchTracksInteractorImpl(
    private val repository: TrackRepository
) : SearchTracksInteractor {
    override fun search(query: String): Flow<Pair<List<Track>?, Boolean>> =
        repository.search(query)
}