package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.PlaylistTrackEntity
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()
private val trackIdsType = object : TypeToken<List<Long>>() {}.type

fun Playlist.toEntity(): PlaylistEntity {
    val trackIdsJson = gson.toJson(trackIds)
    return PlaylistEntity(
        playlistId = playlistId,
        name = name,
        description = description,
        coverPath = coverPath,
        trackIds = trackIdsJson,
        trackCount = trackCount
    )
}

fun PlaylistEntity.toPlaylist(): Playlist {
    val type = trackIdsType
    val ids: List<Long> = try {
        gson.fromJson(trackIds, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return Playlist(
        playlistId = playlistId,
        name = name,
        description = description,
        coverPath = coverPath,
        trackIds = ids,
        trackCount = trackCount
    )
}

fun Track.toPlaylistTrackEntity(): PlaylistTrackEntity = PlaylistTrackEntity(
    trackId = trackId,
    artworkUrl100 = artworkUrl100,
    trackName = trackName,
    artistName = artistName,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    trackTime = trackTime,
    previewUrl = previewUrl
)
