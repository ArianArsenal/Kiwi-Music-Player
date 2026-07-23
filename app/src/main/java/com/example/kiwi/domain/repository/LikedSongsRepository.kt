package com.example.kiwi.domain.repository

import com.example.kiwi.data.local.LikedSongEntity
import com.example.kiwi.data.local.LikedSongsDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikedSongsRepository @Inject constructor(
    private val likedSongsDao: LikedSongsDao
) {
    fun getAllLikedSongs(accountId: String): Flow<List<LikedSongEntity>> {
        return likedSongsDao.getAllLikedSongs(accountId)
    }

    fun isSongLiked(songId: String, accountId: String): Flow<Boolean> {
        return likedSongsDao.isSongLiked(songId, accountId)
    }

    suspend fun toggleLikeState(
        songId: String,
        title: String,
        artistName: String,
        coverImageUrl: String,
        audioUrl: String, // <-- NEW: Added audioUrl parameter
        isCurrentlyLiked: Boolean,
        accountId: String
    ) {
        if (isCurrentlyLiked) {
            likedSongsDao.unlikeSong(songId, accountId)
        } else {
            val entity = LikedSongEntity(
                songId = songId,
                title = title,
                artistName = artistName,
                coverImageUrl = coverImageUrl,
                timestamp = System.currentTimeMillis(),
                accountId = accountId,
                audioUrl = audioUrl
            )
            likedSongsDao.likeSong(entity)
        }
    }
}