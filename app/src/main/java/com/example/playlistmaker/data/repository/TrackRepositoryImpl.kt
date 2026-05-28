package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.mapper.toTrack
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class TrackRepositoryImpl(
    private val api: ItunesApi,
    private val db: AppDatabase
) : TrackRepository {

    override fun search(query: String): Flow<Pair<List<Track>?, Boolean>> = flow {
        try {
            val response = api.search(query)
            val favoriteIds = db.favouriteTracksDao().getAllFavoriteIds().toSet()
            val tracks = response.results.mapNotNull { it.toTrack() }.map { track ->
                if (track.trackId in favoriteIds) track.copy(isFavorite = true) else track
            }
            emit(Pair(tracks, false))
        } catch (e: IOException) {
            emit(Pair(null, true))
        }
    }
}
