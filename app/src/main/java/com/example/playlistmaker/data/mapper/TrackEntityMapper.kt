package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.db.TrackEntity
import com.example.playlistmaker.domain.models.Track

fun Track.toEntity(): TrackEntity = TrackEntity(
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

fun TrackEntity.toTrack(): Track = Track(
    trackId = trackId,
    artworkUrl100 = artworkUrl100,
    trackName = trackName,
    artistName = artistName,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    trackTime = trackTime,
    previewUrl = previewUrl,
    isFavorite = true
)
