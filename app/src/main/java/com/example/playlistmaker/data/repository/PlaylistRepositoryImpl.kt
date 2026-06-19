package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.PlaylistTrackDao
import com.example.playlistmaker.data.mapper.toEntity
import com.example.playlistmaker.data.mapper.toPlaylist
import com.example.playlistmaker.data.mapper.toPlaylistTrackEntity
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlist.toEntity())
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toPlaylist() }
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        val updatedPlaylist = playlist.copy(
            trackIds = playlist.trackIds + track.trackId,
            trackCount = playlist.trackCount + 1
        )
        playlistDao.updatePlaylist(updatedPlaylist.toEntity())
        playlistTrackDao.insertTrack(track.toPlaylistTrackEntity())
    }
}
