package com.example.kiwi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. Search History Table
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val timestamp: Long
)

// 2. Liked Songs Table
@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val timestamp: Long
)

// 3. Downloaded Files Table
@Entity(tableName = "downloaded_files")
data class DownloadedFileEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistName: String,
    val localFilePath: String,
    val downloadedAt: Long
)