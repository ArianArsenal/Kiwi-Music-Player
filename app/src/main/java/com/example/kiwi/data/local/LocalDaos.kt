package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history WHERE accountId = :accountId ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(accountId: String): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchQuery: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE accountId = :accountId")
    suspend fun clearHistory(accountId: String)

    @Query("DELETE FROM search_history WHERE `query` = :searchQuery AND accountId = :accountId")
    suspend fun deleteSearchQuery(searchQuery: String, accountId: String)
}

@Dao
interface LikedSongsDao {
    @Query("SELECT * FROM liked_songs WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getAllLikedSongs(accountId: String): Flow<List<LikedSongEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE songId = :songId AND accountId = :accountId)")
    fun isSongLiked(songId: String, accountId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeSong(song: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE songId = :songId AND accountId = :accountId")
    suspend fun unlikeSong(songId: String, accountId: String)
}

@Dao
interface DownloadedFilesDao {
    @Query("SELECT * FROM downloaded_files WHERE accountId = :accountId ORDER BY downloadedAt DESC")
    fun getAllDownloadedFiles(accountId: String): Flow<List<DownloadedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDownloadedFile(file: DownloadedFileEntity)

    @Query("DELETE FROM downloaded_files WHERE songId = :songId AND accountId = :accountId")
    suspend fun removeDownloadedFile(songId: String, accountId: String)
}