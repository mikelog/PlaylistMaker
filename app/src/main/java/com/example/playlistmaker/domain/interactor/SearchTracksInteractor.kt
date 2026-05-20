package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchTracksInteractor {
    fun search(query: String): Flow<Pair<List<Track>?, Boolean>>
}