package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    // Returns a Flow so the UI updates automatically whenever a song is downloaded or deleted
    @Query("SELECT * FROM SongEntity")
    fun getAllSongs(): Flow<List<SongEntity>>

    // Deletes a specific song using its ID
    @Query("DELETE FROM SongEntity WHERE id = :songId")
    suspend fun deleteSongById(songId: String)

    // Assuming you already have this for saving downloads, but here it is just in case!
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("SELECT * FROM SongEntity")
    fun getAllDownloadedSongs(): Flow<List<SongEntity>>

    // Returns a Flow, so NO suspend keyword is needed
    @Query("SELECT COUNT(*) FROM SongEntity")
    fun getDownloadedSongCount(): Flow<Int>
}