package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(searchQuery: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()

    @Query("DELETE FROM search_history WHERE `query` = :searchQuery")
    suspend fun deleteSearchQuery(searchQuery: String)
}

@Dao
interface LikedSongsDao {
    @Query("SELECT * FROM liked_songs ORDER BY timestamp DESC")
    fun getAllLikedSongs(): Flow<List<LikedSongEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE songId = :songId)")
    fun isSongLiked(songId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeSong(song: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE songId = :songId")
    suspend fun unlikeSong(songId: String)
}

@Dao
interface DownloadedFilesDao {
    @Query("SELECT * FROM downloaded_files ORDER BY downloadedAt DESC")
    fun getAllDownloadedFiles(): Flow<List<DownloadedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDownloadedFile(file: DownloadedFileEntity)

    @Query("DELETE FROM downloaded_files WHERE songId = :songId")
    suspend fun removeDownloadedFile(songId: String)
}