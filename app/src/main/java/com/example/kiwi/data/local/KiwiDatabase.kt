package com.example.kiwi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Notice we do NOT import from com.example.kiwi.data.local.room anymore.
// All entities and DAOs are now safely inside the same 'local' package.

@Database(
    entities = [
        SongEntity::class,
        ChatMessageEntity::class,
        SearchHistoryEntity::class,
        LikedSongEntity::class,
        DownloadedFileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KiwiDatabase : RoomDatabase() {

    // Existing DAOs accessed as properties (matches your DatabaseModule)
    abstract val songDao: SongDao
    abstract val chatDao: ChatDao

    // New DAOs accessed as functions (fixes the "invoke()" error)
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun likedSongsDao(): LikedSongsDao
    abstract fun downloadedFilesDao(): DownloadedFilesDao
}