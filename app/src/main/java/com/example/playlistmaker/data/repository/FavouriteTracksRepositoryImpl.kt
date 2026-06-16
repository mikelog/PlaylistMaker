package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.mapper.toEntity
import com.example.playlistmaker.data.mapper.toTrack
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.FavouriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavouriteTracksRepositoryImpl(
    private val db: AppDatabase
) : FavouriteTracksRepository {

    override suspend fun addTrack(track: Track) {
        db.favouriteTracksDao().insertTrack(track.toEntity())
    }

    override suspend fun deleteTrack(track: Track) {
        db.favouriteTracksDao().deleteTrack(track.toEntity())
    }

    override fun getAllTracks(): Flow<List<Track>> {
        return db.favouriteTracksDao().getAllTracks().map { entities ->
            entities.map { it.toTrack() }
        }
    }
}
