package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.PlaylistTrackDao
import com.example.playlistmaker.data.mapper.toEntity
import com.example.playlistmaker.data.mapper.toPlaylist
import com.example.playlistmaker.data.mapper.toPlaylistTrackEntity
import com.example.playlistmaker.data.mapper.toTrack
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

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

    override fun getPlaylistById(id: Long): Flow<Playlist> {
        return playlistDao.getPlaylistById(id).mapNotNull { it?.toPlaylist() }
    }

    override fun getTracksForPlaylist(trackIds: List<Long>): Flow<List<Track>> = flow {
        val all = playlistTrackDao.getAllTracks().associateBy { it.trackId }
        emit(trackIds.reversed().mapNotNull { id -> all[id]?.toTrack() })
    }

    override suspend fun removeTrackFromPlaylist(trackId: Long, playlist: Playlist) {
        val updatedPlaylist = playlist.copy(
            trackIds = playlist.trackIds.filter { it != trackId },
            trackCount = playlist.trackCount - 1
        )
        playlistDao.updatePlaylist(updatedPlaylist.toEntity())
        val isInAnyPlaylist = playlistDao.getAllPlaylistsSnapshot()
            .any { trackId in it.toPlaylist().trackIds }
        if (!isInAnyPlaylist) {
            playlistTrackDao.deleteTrack(trackId)
        }
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist.playlistId)
        playlist.trackIds.forEach { trackId ->
            val isInOther = playlistDao.getAllPlaylistsSnapshot()
                .any { it.playlistId != playlist.playlistId && trackId in it.toPlaylist().trackIds }
            if (!isInOther) {
                playlistTrackDao.deleteTrack(trackId)
            }
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
