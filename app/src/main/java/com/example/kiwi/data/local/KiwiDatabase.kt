package com.example.kiwi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        ChatMessageEntity::class,
        SearchHistoryEntity::class,
        LikedSongEntity::class,
        DownloadedFileEntity::class,
        UserEntity::class,
        RecentlyPlayedEntity::class,
        FollowEntity::class
    ],
    version = 6, // <-- BUMPED TO 6
    exportSchema = false
)
abstract class KiwiDatabase : RoomDatabase() {

    abstract val songDao: SongDao
    abstract val chatDao: ChatDao
    abstract val userDao: UserDao

    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun likedSongsDao(): LikedSongsDao
    abstract fun downloadedFilesDao(): DownloadedFilesDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun followDao(): FollowDao
}