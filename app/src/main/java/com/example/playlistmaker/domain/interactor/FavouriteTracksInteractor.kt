package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavouriteTracksInteractor {
    suspend fun addTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    fun getAllTracks(): Flow<List<Track>>
}
