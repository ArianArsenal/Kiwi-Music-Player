package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    // Returns a Flow of downloaded songs for the active user
    @Query("SELECT * FROM SongEntity WHERE accountId = :accountId")
    fun getAllSongs(accountId: String): Flow<List<SongEntity>>

    // Deletes a specific downloaded song for the active user
    @Query("DELETE FROM SongEntity WHERE id = :songId AND accountId = :accountId")
    suspend fun deleteSongById(songId: String, accountId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("SELECT * FROM SongEntity WHERE accountId = :accountId")
    fun getAllDownloadedSongs(accountId: String): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM SongEntity WHERE accountId = :accountId")
    fun getDownloadedSongCount(accountId: String): Flow<Int>
}