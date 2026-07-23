package com.example.kiwi.data.local

import androidx.room.Entity

// 1. Search History Table
@Entity(
    tableName = "search_history",
    primaryKeys = ["query", "accountId"]
)
data class SearchHistoryEntity(
    val query: String,
    val timestamp: Long,
    val accountId: String
)

// 2. Liked Songs Table
@Entity(
    tableName = "liked_songs",
    primaryKeys = ["songId", "accountId"]
)
data class LikedSongEntity(
    val songId: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val timestamp: Long,
    val accountId: String,
    val audioUrl: String
)

// 3. Downloaded Files Table
@Entity(
    tableName = "downloaded_files",
    primaryKeys = ["songId", "accountId"]
)
data class DownloadedFileEntity(
    val songId: String,
    val title: String,
    val artistName: String,
    val localFilePath: String,
    val downloadedAt: Long,
    val accountId: String
)