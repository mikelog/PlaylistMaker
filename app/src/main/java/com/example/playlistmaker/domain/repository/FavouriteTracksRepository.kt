package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavouriteTracksRepository {
    suspend fun addTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    fun getAllTracks(): Flow<List<Track>>
}
