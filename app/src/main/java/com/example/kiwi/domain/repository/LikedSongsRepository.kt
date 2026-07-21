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
    fun getAllLikedSongs(): Flow<List<LikedSongEntity>> {
        return likedSongsDao.getAllLikedSongs()
    }

    fun isSongLiked(songId: String): Flow<Boolean> {
        return likedSongsDao.isSongLiked(songId)
    }

    suspend fun toggleLikeState(
        songId: String,
        title: String,
        artistName: String,
        coverImageUrl: String,
        isCurrentlyLiked: Boolean
    ) {
        if (isCurrentlyLiked) {
            likedSongsDao.unlikeSong(songId)
        } else {
            val entity = LikedSongEntity(
                songId = songId,
                title = title,
                artistName = artistName,
                coverImageUrl = coverImageUrl,
                timestamp = System.currentTimeMillis()
            )
            likedSongsDao.likeSong(entity)
        }
    }
}