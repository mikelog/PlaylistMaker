package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(id: Long): Flow<Playlist>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    fun getTracksForPlaylist(trackIds: List<Long>): Flow<List<Track>>
    suspend fun removeTrackFromPlaylist(trackId: Long, playlist: Playlist)
    suspend fun deletePlaylist(playlist: Playlist)
}
