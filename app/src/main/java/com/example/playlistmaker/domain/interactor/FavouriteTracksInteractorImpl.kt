package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.FavouriteTracksRepository
import kotlinx.coroutines.flow.Flow

class FavouriteTracksInteractorImpl(
    private val repository: FavouriteTracksRepository
) : FavouriteTracksInteractor {

    override suspend fun addTrack(track: Track) = repository.addTrack(track)

    override suspend fun deleteTrack(track: Track) = repository.deleteTrack(track)

    override fun getAllTracks(): Flow<List<Track>> = repository.getAllTracks()
}
