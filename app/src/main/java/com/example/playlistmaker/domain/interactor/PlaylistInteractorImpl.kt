package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist) {
        repository.createPlaylist(playlist)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> = repository.getAllPlaylists()

    override fun getPlaylistById(id: Long): Flow<Playlist> = repository.getPlaylistById(id)

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        repository.addTrackToPlaylist(track, playlist)
    }

    override fun getTracksForPlaylist(trackIds: List<Long>): Flow<List<Track>> =
        repository.getTracksForPlaylist(trackIds)

    override suspend fun removeTrackFromPlaylist(trackId: Long, playlist: Playlist) {
        repository.removeTrackFromPlaylist(trackId, playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        repository.deletePlaylist(playlist)
    }
}
