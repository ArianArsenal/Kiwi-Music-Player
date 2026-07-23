package com.example.kiwi.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String,
    val userId: String,
    val lastPlayedTimestamp: Long,
    val playCount: Int = 1
)

// A specialized data class to hold the aggregated artist data
data class TopArtist(
    val artistName: String,
    val coverImageUrl: String,
    val totalPlays: Int
)

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played WHERE userId = :userId AND songId = :songId LIMIT 1")
    suspend fun getSong(songId: String, userId: String): RecentlyPlayedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(song: RecentlyPlayedEntity)

    // Gets the last 20 songs you played
    @Query("SELECT * FROM recently_played WHERE userId = :userId ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getRecentlyPlayed(userId: String): Flow<List<RecentlyPlayedEntity>>

    // Groups your history by artist, counts the plays, and returns your top 10
    @Query("SELECT artistName, MAX(coverImageUrl) as coverImageUrl, SUM(playCount) as totalPlays FROM recently_played WHERE userId = :userId GROUP BY artistName ORDER BY totalPlays DESC LIMIT 10")
    fun getTopArtists(userId: String): Flow<List<TopArtist>>
}