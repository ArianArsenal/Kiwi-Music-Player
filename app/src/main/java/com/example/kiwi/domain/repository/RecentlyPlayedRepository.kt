package com.example.kiwi.domain.repository

import com.example.kiwi.data.local.RecentlyPlayedDao
import com.example.kiwi.data.local.RecentlyPlayedEntity
import com.example.kiwi.data.local.TopArtist
import com.example.kiwi.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentlyPlayedRepository @Inject constructor(
    private val dao: RecentlyPlayedDao
) {
    suspend fun addSongToHistory(song: Song, userId: String) {
        val existing = dao.getSong(song.id, userId)
        val newPlayCount = (existing?.playCount ?: 0) + 1

        val entity = RecentlyPlayedEntity(
            songId = song.id,
            title = song.title,
            artistName = song.artistName,
            coverImageUrl = song.coverImageUrl,
            audioUrl = song.audioUrl,
            userId = userId,
            lastPlayedTimestamp = System.currentTimeMillis(),
            playCount = newPlayCount
        )
        dao.insertOrUpdate(entity)
    }

    fun getRecentlyPlayed(userId: String): Flow<List<Song>> {
        return dao.getRecentlyPlayed(userId).map { entities ->
            entities.map {
                Song(it.songId, it.title, it.artistName, it.coverImageUrl, it.audioUrl)
            }
        }
    }

    fun getTopArtists(userId: String): Flow<List<TopArtist>> {
        return dao.getTopArtists(userId)
    }
}