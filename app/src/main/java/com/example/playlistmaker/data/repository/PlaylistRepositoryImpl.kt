package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.mapper.toEntity
import com.example.playlistmaker.data.mapper.toPlaylist
import com.example.playlistmaker.data.mapper.toPlaylistTrackEntity
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val db: AppDatabase
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist) {
        db.playlistDao().insertPlaylist(playlist.toEntity())
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        db.playlistDao().updatePlaylist(playlist.toEntity())
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return db.playlistDao().getAllPlaylists().map { entities ->
            entities.map { it.toPlaylist() }
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        val updatedIds = playlist.trackIds + track.trackId
        val updatedPlaylist = playlist.copy(
            trackIds = updatedIds,
            trackCount = playlist.trackCount + 1
        )
        db.playlistDao().updatePlaylist(updatedPlaylist.toEntity())
        db.playlistTrackDao().insertTrack(track.toPlaylistTrackEntity())
    }
}
