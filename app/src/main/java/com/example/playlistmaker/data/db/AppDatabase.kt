package com.example.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favouriteTracksDao(): FavouriteTracksDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao
}
